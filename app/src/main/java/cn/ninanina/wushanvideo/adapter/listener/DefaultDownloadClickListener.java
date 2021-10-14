package cn.ninanina.wushanvideo.adapter.listener;

import android.app.Activity;
import android.app.Dialog;

import java.io.File;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.bean.common.DownloadInfo;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.service.DownloadService;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.util.DBHelper;
import cn.ninanina.wushanvideo.util.DialogManager;
import cn.ninanina.wushanvideo.util.FileUtil;
import cn.ninanina.wushanvideo.util.ToastUtil;

public class DefaultDownloadClickListener implements DownloadClickListener {
    DownloadService downloadService;
    private Activity activity;
    public boolean showMessage = true;
    private static int downloadCount = 0;//近期下载的次数

    public DefaultDownloadClickListener(DownloadService downloadService, Activity activity) {
        this.downloadService = downloadService;
        this.activity = activity;
    }

    @Override
    public void onClick(VideoDetail videoDetail) {
        DBHelper dbHelper = WushanApp.getInstance().getDbHelper();
        String path = FileUtil.getVideoDir().getAbsolutePath() + "/" + FileUtil.getVideoFileName(videoDetail);
        File file = new File(path);
        if (file.exists()) {
            if (showMessage) ToastUtil.show("视频已下载");
            if (!dbHelper.downloaded(videoDetail.getId())) dbHelper.saveVideo(videoDetail);
            return;
        }
        DownloadService downloadService = MainActivity.getInstance().downloadService;
        DownloadInfo downloadInfo = downloadService.getTasks().get(videoDetail.getSrc());
        if (downloadInfo != null) {
            int status = downloadInfo.getStatus();
            if (status != DownloadInfo.running)
                MainActivity.getInstance().downloadService.resumeTask(downloadInfo);
            return;
        }
        if (showMessage) ToastUtil.show("开始下载");
        VideoPresenter.getInstance().downloadVideo(videoDetail.getId());
        downloadService.newTask(videoDetail, path);
        downloadCount++;
        long lastShowShareTime = WushanApp.getConstants().getLong("lastShowShare", 0);
        if (downloadCount >= 3 && System.currentTimeMillis() - lastShowShareTime > 6 * 3600 * 1000) {
            DialogManager.getInstance().newShareDialog(activity).show();
        }
    }
}
