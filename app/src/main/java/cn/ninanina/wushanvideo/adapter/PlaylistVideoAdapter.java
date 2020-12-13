package cn.ninanina.wushanvideo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.ads.AdView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.listener.VideoClickListener;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.util.CommonUtils;
import cn.ninanina.wushanvideo.util.DBHelper;

public class PlaylistVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public PlaylistVideoAdapter(List<VideoDetail> videoDetails, VideoClickListener listener, VideoClickListener optionsClickListener) {
        this.videoDetails = videoDetails;
        this.listener = listener;
        this.optionsClickListener = optionsClickListener;
    }

    private List<VideoDetail> videoDetails;
    private VideoClickListener listener;
    private VideoClickListener optionsClickListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_playlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VideoHolder videoHolder = (VideoHolder) holder;
        VideoDetail videoDetail = videoDetails.get(position);
        videoHolder.cover.setAspectRatio(1.78f);
        videoHolder.cover.setImageURI(videoDetail.getCoverUrl());
        if (StringUtils.isEmpty(videoDetail.getTitleZh()))
            videoHolder.videoTitle.setText(videoDetail.getTitle());
        else videoHolder.videoTitle.setText(videoDetail.getTitleZh());
        videoHolder.videoViews.setText(CommonUtils.getViewsString(videoDetail.getViewed() / 100));
        videoHolder.videoDuration.setText(CommonUtils.getDurationString(videoDetail.getDuration()));
        DBHelper dbHelper = WushanApp.getInstance().getDbHelper();
        if (dbHelper.downloaded(videoDetail.getId())) {
            videoHolder.downloadedIcon.setVisibility(View.VISIBLE);
            videoHolder.downloadedText.setVisibility(View.VISIBLE);
        } else if (MainActivity.getInstance().downloadService.getTasks().containsKey(videoDetail.getSrc())) {
            videoHolder.downloadedText.setVisibility(View.VISIBLE);
            videoHolder.downloadedText.setText("正在下载");
        }
        int viewedCount = DataHolder.getInstance().viewedCount(videoDetail.getId());
        if (viewedCount > 0) {
            videoHolder.eye.setVisibility(View.VISIBLE);
            videoHolder.viewed.setText(viewedCount + "次");
        }
        videoHolder.itemView.setOnClickListener(v -> listener.onClick(videoDetails.get(holder.getLayoutPosition())));
        videoHolder.videoMore.setOnClickListener(v -> optionsClickListener.onClick(videoDetails.get(holder.getLayoutPosition())));
        videoHolder.itemView.setOnLongClickListener(v -> {
            optionsClickListener.onClick(videoDetails.get(holder.getLayoutPosition()));
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return videoDetails.size();
    }

    public void updateOne(long videoId) {
        for (int i = 0; i < videoDetails.size(); i++) {
            VideoDetail videoDetail = videoDetails.get(i);
            if (videoDetail.getId() == videoId) {
                notifyItemChanged(i);
            }
        }
    }

    public void deleteOne(long videoId) {
        int toDelete = -1;
        for (int i = 0; i < videoDetails.size(); i++) {
            VideoDetail videoDetail = videoDetails.get(i);
            if (videoDetail.getId() == videoId) {
                toDelete = i;
            }
        }
        if (toDelete != -1) {
            videoDetails.remove(toDelete);
            notifyItemRemoved(toDelete);
        }
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
        @BindView(R.id.eye)
        ImageView eye;
        @BindView(R.id.viewed)
        TextView viewed;
        @BindView(R.id.video_action)
        FrameLayout videoMore;
        @BindView(R.id.downloaded_icon)
        ImageView downloadedIcon;
        @BindView(R.id.downloaded_text)
        TextView downloadedText;

        VideoHolder(View itemView) {
            super(itemView);
            if (!(itemView instanceof AdView))
                ButterKnife.bind(this, itemView);
        }
    }
}
