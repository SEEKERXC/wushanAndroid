package cn.ninanina.wushanvideo.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadReceiver;
import com.arialyy.aria.core.task.DownloadTask;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.bean.common.DownloadInfo;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.ui.video.DetailFragment;
import cn.ninanina.wushanvideo.ui.video.DownloadedFragment;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;
import cn.ninanina.wushanvideo.util.CommonUtils;
import cn.ninanina.wushanvideo.util.FileUtil;
import cn.ninanina.wushanvideo.util.PermissionUtils;
import cn.ninanina.wushanvideo.util.ToastUtil;

public class DownloadService extends Service {
    private DownloadBinder binder = new DownloadBinder();

    private Map<String, DownloadInfo> tasks = new HashMap<>();
    private String apkUrl;
    private String apkName;
    private boolean installed = false;

    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    public void newTask(VideoDetail videoDetail, String path) {
        long taskId = Aria.download(this)
                .load(videoDetail.getSrc())
                .setFilePath(path)
                .ignoreFilePathOccupy()
                .create();
        if (taskId == -1) {
            ToastUtil.show("创建下载失败，请稍后重试");
        } else {
            DownloadInfo downloadInfo = new DownloadInfo();
            downloadInfo.setVideo(videoDetail);
            downloadInfo.setTaskId(taskId);
            downloadInfo.setUrl(videoDetail.getSrc());
            downloadInfo.setPath(path);
            File file = new File(path);
            downloadInfo.setFileName(file.getName().substring(0, file.getName().length() - 4));
            downloadInfo.setProgress("等待中");
            downloadInfo.setPercentage(0);
            downloadInfo.setSpeed("");
            downloadInfo.setStatus(DownloadInfo.waiting);
            tasks.put(videoDetail.getSrc(), downloadInfo);
        }
    }

    public void downloadApk(String apkUrl, String name) {
        Aria.download(this)
                .load(apkUrl)
                .setFilePath(FileUtil.getDownloadDir().getAbsolutePath() + "/" + name)
                .ignoreFilePathOccupy()
                .create();
        this.apkUrl = apkUrl;
        this.apkName = name;
    }

    public void resumeTask(DownloadInfo downloadInfo) {
        if (tasks.containsValue(downloadInfo)) {
            Aria.download(this)
                    .load(downloadInfo.getTaskId())
                    .resume(true);
            ToastUtil.show("开始下载");
        }
    }

    public Map<String, DownloadInfo> getTasks() {
        return tasks;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Aria.download(this).register();
        Aria.get(this).getDownloadConfig().setConvertSpeed(false).setReTryInterval(3000).setReTryNum(20);
        PermissionUtils.requestReadWritePermissions(MainActivity.getInstance());
    }

    @Download.onTaskStart
    public void onTaskStart(DownloadTask task) {
        if (task.getKey().equals(apkUrl)) {
            ToastUtil.show("开始下载APK");
        }
    }

    @Download.onTaskStop
    public void onTaskStop(DownloadTask task) {
        DownloadInfo downloadInfo = tasks.get(task.getKey());
        if (downloadInfo == null) return;
        downloadInfo.setStatus(DownloadInfo.pause);
        downloadInfo.setSpeed("暂停下载");
    }

    @Download.onWait
    public void onWait(DownloadTask task) {
        DownloadInfo downloadInfo = tasks.get(task.getKey());
        if (downloadInfo == null) return;
        downloadInfo.setSpeed("");
        downloadInfo.setStatus(DownloadInfo.waiting);
    }

    @Download.onTaskCancel
    public void onTaskCancel(DownloadTask task) {
        tasks.remove(task.getKey());
    }

    @Download.onTaskFail
    public void onTaskFail(DownloadTask task) {
        if (task == null) return;
        ToastUtil.show(task.getDownloadEntity().getFileName() + " 下载失败");
        DownloadInfo downloadInfo = tasks.get(task.getKey());
        if (downloadInfo == null) return;
        downloadInfo.setSpeed("");
        downloadInfo.setProgress("下载失败，点击重试");
        downloadInfo.setStatus(DownloadInfo.error);
    }

    @Download.onTaskComplete
    public void onTaskComplete(DownloadTask task) {
        if (task == null) return;
        if (tasks.containsKey(task.getKey())) {
            if (tasks.get(task.getKey()) == null || tasks.get(task.getKey()).getVideo() == null)
                return;
            VideoDetail videoDetail = tasks.get(task.getKey()).getVideo();
            WushanApp.getInstance().getDbHelper().saveVideo(videoDetail);
            tasks.remove(task.getKey());
            ToastUtil.show(task.getDownloadEntity().getFileName() + " 下载完成");
            Handler handler = DownloadedFragment.handler;
            if (handler != null) {
                Message message = new Message();
                message.what = DownloadedFragment.update;
                handler.sendMessage(message);
            }
            if (!MainActivity.getInstance().videoActivityStack.empty()) {
                VideoDetailActivity detailActivity = null;
                for (VideoDetailActivity activity : MainActivity.getInstance().videoActivityStack) {
                    if (activity.video.getSrc().equals(task.getKey())) detailActivity = activity;
                }
                if (detailActivity == null) return;
                DetailFragment fragment = (DetailFragment) detailActivity.fragments.get(0);
                Handler handler1 = fragment.handler;
                if (handler1 != null && task.getKey().equals(fragment.videoDetail.getSrc())) {
                    Message message = new Message();
                    message.what = DetailFragment.downloadFinish;
                    handler1.sendMessage(message);
                }
            }
        }
        if (task.getKey().equals(apkUrl)) {
            File file = new File(FileUtil.getDownloadDir().getAbsolutePath() + "/" + apkName);
            Intent install = new Intent();
            install.setAction(Intent.ACTION_VIEW);
            install.addCategory(Intent.CATEGORY_DEFAULT);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(WushanApp.getInstance(), "cn.ninanina.wushanvideo.fileProvider", file);
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
                install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            }
            if (MainActivity.getInstance() != null && !installed) {
                MainActivity.getInstance().startActivity(install);
                installed = true;
            }
        }
    }

    @Download.onTaskRunning
    public void onTaskRunning(DownloadTask task) {
        //发送给下载页面
        long len = task.getFileSize();
        long current = task.getCurrentProgress();

        DownloadInfo downloadInfo = tasks.get(task.getKey());
        if (downloadInfo == null) return;
        downloadInfo.setStatus(DownloadInfo.running);
        downloadInfo.setSpeed(FileUtil.getSize(task.getSpeed()) + "/s");
        downloadInfo.setProgress(FileUtil.getSize(current) + "/" + FileUtil.getSize(len));
        downloadInfo.setPercentage(task.getPercent());

        //发送给视频页面
        String src = task.getKey();
        if (!MainActivity.getInstance().videoActivityStack.empty()) {
            VideoDetailActivity activity = null;
            for (VideoDetailActivity detailActivity : MainActivity.getInstance().videoActivityStack) {
                if (CommonUtils.isSrcValid(detailActivity.video.getSrc()) && detailActivity.video.getSrc().equals(task.getKey()))
                    activity = detailActivity;
            }
            if (activity == null) return;
            DetailFragment fragment = (DetailFragment) activity.fragments.get(0);
            Handler handler = fragment.handler;
            if (handler != null && src.equals(fragment.videoDetail.getSrc())) {
                Message message = new Message();
                message.what = DetailFragment.downloading;
                handler.sendMessage(message);
            }
        }
    }
}
