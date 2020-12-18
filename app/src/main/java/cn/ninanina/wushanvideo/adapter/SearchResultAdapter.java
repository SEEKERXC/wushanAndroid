package cn.ninanina.wushanvideo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.ads.AdView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.listener.VideoClickListener;
import cn.ninanina.wushanvideo.model.bean.common.VideoDuration;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.util.CommonUtils;

public class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public SearchResultAdapter(List<VideoDetail> allData, VideoClickListener listener, VideoClickListener optionsClickListener) {
        this.allData = allData;
        this.showData = new ArrayList<>();
        this.listener = listener;
        this.optionsClickListener = optionsClickListener;
    }

    List<VideoDetail> showData; //根据duration过滤出来的数据
    List<VideoDetail> allData; //所有数据
    private VideoClickListener listener;
    private VideoClickListener optionsClickListener;
    public VideoDuration duration = VideoDuration.ALL;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_search, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VideoDetail videoDetail = showData.get(position);
        VideoHolder videoHolder = (VideoHolder) holder;
        videoHolder.cover.setAspectRatio(1.78f);
        videoHolder.cover.setImageURI(videoDetail.getCoverUrl());
        if (StringUtils.isEmpty(videoDetail.getTitleZh()))
            videoHolder.videoTitle.setText(videoDetail.getTitle());
        else videoHolder.videoTitle.setText(videoDetail.getTitleZh());
        videoHolder.videoViews.setText(CommonUtils.getViewsString(videoDetail.getViewed() / 100));
        videoHolder.videoDuration.setText(CommonUtils.getDurationString(videoDetail.getDuration()));
        videoHolder.collect.setText(String.valueOf(videoDetail.getCollected()));
        videoHolder.download.setText(String.valueOf(videoDetail.getDownloaded()));
        videoHolder.itemView.setOnClickListener(v -> listener.onClick(videoDetail));
        videoHolder.videoMore.setOnClickListener(v -> optionsClickListener.onClick(videoDetail));
        videoHolder.itemView.setOnLongClickListener(v -> {
            optionsClickListener.onClick(videoDetail);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return showData.size();
    }

    public void append(List<VideoDetail> data) {
        allData.addAll(data);
        int count = showData.size();
        for (VideoDetail videoDetail : data)
            if (matchDuration(videoDetail)) showData.add(videoDetail);
        notifyItemRangeInserted(count, showData.size() - count);
    }

    public void reset(List<VideoDetail> data) {
        allData.clear();
        int size = showData.size();
        showData.clear();
        notifyItemRangeRemoved(0, size);
        allData.addAll(data);
        for (VideoDetail videoDetail : allData) {
            if (matchDuration(videoDetail)) showData.add(videoDetail);
        }
        notifyDataSetChanged();
    }

    public VideoDuration getDuration() {
        return duration;
    }

    public void setDuration(VideoDuration duration) {
        if (this.duration == duration) return;
        this.duration = duration;
        showData.clear();
        for (VideoDetail videoDetail : allData)
            if (matchDuration(videoDetail)) showData.add(videoDetail);
        notifyDataSetChanged();
    }

    private boolean matchDuration(VideoDetail videoDetail) {
        int seconds = CommonUtils.getDurationSeconds(videoDetail.getDuration());
        int min = 0, max = Integer.MAX_VALUE;
        switch (duration) {
            case ALL:
                break;
            case SHORT:
                max = 20 * 60;
                break;
            case MIDDLE:
                min = 20 * 60;
                max = 60 * 60;
                break;
            case LONG:
                min = 60 * 60;
                break;
        }
        return seconds >= min && seconds <= max;
    }

    static final class VideoHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.video_cover)
        SimpleDraweeView cover;
        @BindView(R.id.video_title)
        TextView videoTitle;
        @BindView(R.id.video_views)
        TextView videoViews;
        @BindView(R.id.video_duration)
        TextView videoDuration;
        @BindView(R.id.video_collect)
        TextView collect;
        @BindView(R.id.video_download)
        TextView download;
        @BindView(R.id.video_action)
        FrameLayout videoMore;

        private VideoHolder(View itemView) {
            super(itemView);
            if (!(itemView instanceof AdView))
                ButterKnife.bind(this, itemView);
        }
    }
}
