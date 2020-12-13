package cn.ninanina.wushanvideo.adapter.listener;

import java.io.File;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.bean.common.DownloadInfo;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.service.DownloadService;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.util.FileUtil;
import cn.ninanina.wushanvideo.util.ToastUtil;

public class DefaultDownloadClickListener implements DownloadClickListener {
    DownloadService downloadService;
    public boolean showMessage = true;

    public DefaultDownloadClickListener(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @Override
    public void onClick(VideoDetail videoDetail) {
        String path = FileUtil.getVideoDir().getAbsolutePath() + "/" + FileUtil.getVideoFileName(videoDetail);
        File file = new File(path);
        if (file.exists()) {
            if (showMessage) ToastUtil.show("视频已下载");
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
        VideoPresenter.getInstance().downloadVideo(WushanApp.getInstance().getApplicationContext(), videoDetail.getId());
        downloadService.newTask(videoDetail, path);
    }
}
