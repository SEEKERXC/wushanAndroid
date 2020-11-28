package cn.ninanina.wushanvideo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.ads.AdView;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.listener.VideoClickListener;
import cn.ninanina.wushanvideo.adapter.listener.VideoOptionClickListener;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.util.TimeUtil;

public class DownloadVideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<VideoDetail> dataList;
    private VideoClickListener listener;
    private VideoOptionClickListener optionsClickListener;

    public DownloadVideoListAdapter(List<VideoDetail> dataList, VideoClickListener itemClickListener, VideoOptionClickListener optionsClickListener) {
        this.dataList = dataList;
        this.listener = itemClickListener;
        this.optionsClickListener = optionsClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoCardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_card_download, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VideoCardHolder videoCardHolder = (VideoCardHolder) holder;
        videoCardHolder.cover.setAspectRatio(1.78f);
        VideoDetail videoDetail = dataList.get(position);
        if (videoDetail.getId() != null) {
            videoCardHolder.cover.setImageURI(videoDetail.getCoverUrl());
            if (StringUtils.isEmpty(videoDetail.getTitleZh()))
                videoCardHolder.videoTitle.setText(videoDetail.getTitle());
            else videoCardHolder.videoTitle.setText(videoDetail.getTitleZh());
            String duration = videoDetail.getDuration().replace(" ", "\0").replace("min", "分钟").replace("h", "小时").replace("sec", "秒");
            videoCardHolder.videoDuration.setText(duration);
        }
        videoCardHolder.updateTime.setText(TimeUtil.getFullTime(videoDetail.getUpdateTime()));
        videoCardHolder.videoCard.setOnClickListener(v -> listener.onVideoClicked(dataList.get(holder.getLayoutPosition())));
        videoCardHolder.videoMore.setOnClickListener(v -> optionsClickListener.onVideoOptionClicked(dataList.get(holder.getLayoutPosition())));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static final class VideoCardHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.video_cover)
        SimpleDraweeView cover;
        @BindView(R.id.video_title)
        TextView videoTitle;
        @BindView(R.id.updateTime)
        TextView updateTime;
        @BindView(R.id.video_duration)
        TextView videoDuration;
        @BindView(R.id.video_action)
        ImageButton videoMore;
        @BindView(R.id.video_card)
        CardView videoCard;

        private VideoCardHolder(View itemView) {
            super(itemView);
            if (!(itemView instanceof AdView))
                ButterKnife.bind(this, itemView);
        }
    }
}
