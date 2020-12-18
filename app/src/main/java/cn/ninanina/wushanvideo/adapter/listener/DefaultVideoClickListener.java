package cn.ninanina.wushanvideo.adapter.listener;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.util.CollectionUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import cn.ninanina.wushanvideo.model.bean.video.Tag;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;
import cn.ninanina.wushanvideo.util.DialogManager;
import cn.ninanina.wushanvideo.util.PlayTimeManager;

/**
 * 进入视频详情页面
 */
public class DefaultVideoClickListener implements VideoClickListener {
    public DefaultVideoClickListener(Context context) {
        this.context = context;
    }

    private Context context;

    @Override
    public void onClick(VideoDetail videoDetail) {
        if (PlayTimeManager.getTodayWatchTime() > 60 * 60) {
            DialogManager.getInstance().newWatchPromptDialog(context, videoDetail, this).show();
        } else {
            playVideo(videoDetail);
        }
    }

    @Override
    public void playVideo(VideoDetail videoDetail) {
        Intent intent = new Intent(context, VideoDetailActivity.class);
        intent.putExtra("video", videoDetail);
        context.startActivity(intent);
    }
}
