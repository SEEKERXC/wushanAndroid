package cn.ninanina.wushanvideo.adapter.listener;

import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.service.DownloadService;
import cn.ninanina.wushanvideo.util.FileUtil;

public class DefaultDownloadClickListener implements DownloadClickListener {
    DownloadService downloadService;

    public DefaultDownloadClickListener(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @Override
    public void onClick(VideoDetail videoDetail) {
        String fileName;
        if (StringUtils.isEmpty(videoDetail.getTitleZh())) fileName = videoDetail.getTitle();
        else fileName = videoDetail.getTitleZh();
        fileName = fileName.replaceAll("/", "").trim() + ".mp4";
        String path = FileUtil.getVideoDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        if (file.exists() || downloadService.getTasks().containsKey(videoDetail.getSrc())) {
            Toast.makeText(WushanApp.getInstance().getApplicationContext(), "视频已下载", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(WushanApp.getInstance().getApplicationContext(), "开始下载", Toast.LENGTH_SHORT).show();
        VideoPresenter.getInstance().downloadVideo(WushanApp.getInstance().getApplicationContext(), videoDetail.getId());
        downloadService.newTask(videoDetail, path);
    }
}
