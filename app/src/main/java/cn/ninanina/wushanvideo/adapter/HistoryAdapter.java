package cn.ninanina.wushanvideo.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.model.bean.common.Pair;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.model.bean.video.VideoUserViewed;
import cn.ninanina.wushanvideo.util.TimeUtil;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    int typeTime = 0;
    int typeVideo = 1;
    int typeLoading = 2;
    List<Object> dataList;

    public HistoryAdapter(List<Object> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == typeVideo) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_history, parent, false);
            return new VideoHolder(view);
        } else if (viewType == typeTime) {
            TextView textView = new TextView(parent.getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(40, 20, 10, 20);
            textView.setLayoutParams(layoutParams);
            return new OtherHolder(textView);
        } else if (viewType == typeLoading) {
            TextView textView = new TextView(parent.getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 20, 0, 20);
            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            return new OtherHolder(textView);
        }
        return new OtherHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == typeTime) {
            OtherHolder timeHolder = (OtherHolder) holder;
            ((TextView) timeHolder.itemView).setText((String) dataList.get(position));
        } else if (getItemViewType(position) == typeVideo) {
            VideoHolder videoHolder = (VideoHolder) holder;
            Pair<VideoUserViewed, VideoDetail> pair = (Pair<VideoUserViewed, VideoDetail>) dataList.get(position);
            VideoUserViewed viewed = pair.getFirst();
            VideoDetail videoDetail = pair.getSecond();
            videoHolder.cover.setAspectRatio(1.78f);
            videoHolder.cover.setImageURI(videoDetail.getCoverUrl());
            if (StringUtils.isEmpty(videoDetail.getTitleZh()))
                videoHolder.videoTitle.setText(videoDetail.getTitle());
            else videoHolder.videoTitle.setText(videoDetail.getTitleZh());
            videoHolder.viewCount.setText("观看次数：" + viewed.getViewCount());
            videoHolder.videoDuration.setText(videoDetail.getDuration());
            videoHolder.lastTime.setText("上次观看时间：" + TimeUtil.getTime(viewed.getTime()));
        } else if (getItemViewType(position) == typeLoading) {
            Boolean loadingFinished = (Boolean) dataList.get(position);
            OtherHolder loadingHolder = (OtherHolder) holder;
            if (loadingFinished)
                ((TextView) loadingHolder.itemView).setText("~ 没有更多了 ~");
            else ((TextView) loadingHolder.itemView).setText("加载中...");
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object o = dataList.get(position);
        if (o instanceof Pair) return typeVideo;
        else if (o instanceof String) return typeTime;
        else if (o instanceof Boolean) return typeLoading;
        return 0;
    }

    public void insert(List<Object> data) {
        dataList.remove(dataList.size() - 1);
        notifyItemRangeRemoved(dataList.size() - 1, 1);
        int count = dataList.size();
        dataList.addAll(data);
        notifyItemRangeInserted(count, data.size());
    }

    static final class VideoHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.video_cover)
        SimpleDraweeView cover;
        @BindView(R.id.video_title)
        TextView videoTitle;
        @BindView(R.id.viewCount)
        TextView viewCount;
        @BindView(R.id.video_duration)
        TextView videoDuration;
        @BindView(R.id.lastTime)
        TextView lastTime;
        @BindView(R.id.video_action)
        ImageButton videoMore;

        public VideoHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class OtherHolder extends RecyclerView.ViewHolder {

        public OtherHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
