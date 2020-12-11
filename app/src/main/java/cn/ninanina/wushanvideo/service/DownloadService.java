package cn.ninanina.wushanvideo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.task.DownloadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.bean.common.DownloadInfo;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.util.FileUtil;
import cn.ninanina.wushanvideo.util.PermissionUtils;

public class DownloadService extends Service {
    private DownloadBinder binder = new DownloadBinder();

    private Map<String, DownloadInfo> tasks = new HashMap<>();

    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    public void newTask(VideoDetail videoDetail, String path) {
        long taskId = Aria.download(this)
                .load(videoDetail.getSrc())
                .setFilePath(path)
                .create();
        if (taskId == -1) {
            Toast.makeText(getApplicationContext(), "创建下载失败，请稍后重试", Toast.LENGTH_SHORT).show();
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
            downloadInfo.setSpeed("等待中");
            downloadInfo.setStatus(DownloadInfo.waiting);
            tasks.put(videoDetail.getSrc(), downloadInfo);
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
        Aria.get(this).getDownloadConfig().setConvertSpeed(false);
        PermissionUtils.requestReadWritePermissions(MainActivity.getInstance());
    }

    @Download.onTaskStart
    public void onTaskStart(DownloadTask task) {

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
        downloadInfo.setSpeed("等待中");
        downloadInfo.setStatus(DownloadInfo.waiting);
    }

    @Download.onTaskCancel
    public void onTaskCancel(DownloadTask task) {
        tasks.remove(task.getKey());
    }

    @Download.onTaskFail
    public void onTaskFail(DownloadTask task) {
        Toast.makeText(getApplicationContext(), task.getDownloadEntity().getFileName() + " 下载失败", Toast.LENGTH_SHORT).show();
        DownloadInfo downloadInfo = tasks.get(task.getKey());
        if (downloadInfo == null) return;
        downloadInfo.setSpeed("下载失败");
        downloadInfo.setStatus(DownloadInfo.error);
    }

    @Download.onTaskComplete
    public void onTaskComplete(DownloadTask task) {
        if (task == null || tasks.get(task.getKey()) == null) return;
        VideoDetail videoDetail = tasks.get(task.getKey()).getVideo();
        WushanApp.getInstance().getDbHelper().saveVideo(videoDetail);
        tasks.remove(task.getKey());
        Toast.makeText(getApplicationContext(), task.getDownloadEntity().getFileName() + " 下载完成", Toast.LENGTH_SHORT).show();

    }

    @Download.onTaskRunning
    public void onTaskRunning(DownloadTask task) {
        long len = task.getFileSize();
        long current = task.getCurrentProgress();

        DownloadInfo downloadInfo = tasks.get(task.getKey());
        if (downloadInfo == null) return;
        downloadInfo.setStatus(DownloadInfo.running);
        downloadInfo.setSpeed(FileUtil.getSize(task.getSpeed()) + "/s");
        downloadInfo.setProgress(FileUtil.getSize(current) + "/" + FileUtil.getSize(len));
        downloadInfo.setPercentage(task.getPercent());
    }
}
