package cn.ninanina.wushanvideo.adapter.listener;

import android.content.Context;
import android.content.Intent;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import cn.ninanina.wushanvideo.model.bean.video.Tag;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;

/**
 * 进入视频详情页面
 */
public class DefaultVideoClickListener implements VideoClickListener {
    public DefaultVideoClickListener(Context context) {
        this.context = context;
    }

    private Context context;

    @Override
    public void onVideoClicked(VideoDetail videoDetail) {
        Intent intent = new Intent(context, VideoDetailActivity.class);
        intent.putExtra("id", videoDetail.getId());
        intent.putExtra("title", videoDetail.getTitle());
        intent.putExtra("titleZh", videoDetail.getTitleZh());
        intent.putExtra("viewed", videoDetail.getViewed());
        intent.putExtra("coverUrl", videoDetail.getCoverUrl());
        ArrayList<String> tags = new ArrayList<>();
        for (Tag tag : videoDetail.getTags()) {
            if (!StringUtils.isEmpty(tag.getTagZh()) && !tags.contains(tag.getTagZh()))
                tags.add(tag.getTagZh());
            else tags.add(tag.getTag());
        }
        if (videoDetail.getTags().isEmpty()) tags.add("无标签");
        intent.putStringArrayListExtra("tags", tags);
        context.startActivity(intent);
    }
}
