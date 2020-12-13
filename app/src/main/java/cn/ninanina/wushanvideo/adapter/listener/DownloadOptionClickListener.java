package cn.ninanina.wushanvideo.adapter.listener;

import android.content.Context;

import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.util.DialogManager;

public class DownloadOptionClickListener implements VideoClickListener {
    public DownloadOptionClickListener(Context context) {
        this.context = context;
    }

    private Context context;

    @Override
    public void onClick(VideoDetail videoDetail) {
        DialogManager.getInstance().newDownloadOptionDialog(context, videoDetail).show();
    }
}
