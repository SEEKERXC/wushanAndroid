package cn.ninanina.wushanvideo.adapter.listener;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.util.DialogManager;

/**
 * 展示视频选项
 */
public class DefaultVideoOptionClickListener implements VideoOptionClickListener {
    private Context context;

    public DefaultVideoOptionClickListener(Context context) {
        this.context = context;
    }

    @Override
    public void onVideoOptionClicked(VideoDetail videoDetail) {
        AlertDialog dialog = DialogManager.getInstance().newVideoOptionDialog(context, videoDetail);
        dialog.show();
    }
}
