package cn.ninanina.wushanvideo.adapter.listener;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.util.DialogManager;

/**
 * 展示视频选项
 */
public class DefaultVideoOptionClickListener implements VideoClickListener {
    private Activity activity;

    public DefaultVideoOptionClickListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(VideoDetail videoDetail) {
        AlertDialog dialog = DialogManager.getInstance().newVideoOptionDialog(activity, videoDetail);
        dialog.show();
    }
}
