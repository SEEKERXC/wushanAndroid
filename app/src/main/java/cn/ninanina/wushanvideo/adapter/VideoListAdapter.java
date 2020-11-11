package cn.ninanina.wushanvideo.adapter;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoCardHolder> {
    private List<VideoDetail> videoDetails;
    private List<View.OnClickListener> listeners;

    public VideoListAdapter(List<VideoDetail> videoDetails, List<View.OnClickListener> listeners) {
        this.videoDetails = videoDetails;
        this.listeners = listeners;
    }

    @NonNull
    @Override
    public VideoCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoCardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoListAdapter.VideoCardHolder holder, int position) {
        VideoDetail videoDetail = videoDetails.get(position);
        holder.cover.setAspectRatio(1.78f);
        holder.cover.setImageURI(videoDetail.getCoverUrl());
        holder.videoTitle.setText(videoDetail.getTitle());
        String views = String.valueOf(videoDetail.getViewed() / 100);
        holder.videoViews.setText(views);
        String duration = videoDetail.getDuration().replace("min", "分钟").replace("h", "小时").replace("sec", "秒");
        holder.videoDuration.setText(duration);

        holder.cover.setOnClickListener(listeners.get(position));
        holder.videoTitle.setOnClickListener(listeners.get(position));
    }

    @Override
    public int getItemCount() {
        return videoDetails.size();
    }

    static class VideoCardHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.video_cover)
        SimpleDraweeView cover;
        @BindView(R.id.video_title)
        TextView videoTitle;
        @BindView(R.id.video_views)
        TextView videoViews;
        @BindView(R.id.video_duration)
        TextView videoDuration;
        @BindView(R.id.video_tags)
        LinearLayout videoTags;
        @BindView(R.id.video_dislike)
        ImageButton videoDislike;

        private VideoCardHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
