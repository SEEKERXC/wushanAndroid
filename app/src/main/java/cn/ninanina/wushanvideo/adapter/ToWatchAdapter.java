package cn.ninanina.wushanvideo.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.listener.ToWatchClickListener;
import cn.ninanina.wushanvideo.adapter.listener.VideoClickListener;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.common.Pair;
import cn.ninanina.wushanvideo.model.bean.video.ToWatch;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.model.bean.video.VideoUserViewed;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.util.CommonUtils;
import cn.ninanina.wushanvideo.util.DBHelper;
import cn.ninanina.wushanvideo.util.TimeUtil;

public class ToWatchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int typeTime = 0;
    private static final int typeVideo = 1;
    private static final int typeLoading = 2;
    List<Object> dataList;
    private VideoClickListener videoClickListener;
    private ToWatchClickListener optionClickListener;

    public ToWatchAdapter(List<Object> dataList, VideoClickListener videoClickListener, ToWatchClickListener optionClickListener) {
        this.dataList = dataList;
        this.videoClickListener = videoClickListener;
        this.optionClickListener = optionClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == typeVideo) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_playlist, parent, false);
            return new PlaylistVideoAdapter.VideoHolder(view);
        } else if (viewType == typeTime) {
            TextView textView = new TextView(parent.getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(40, 20, 10, 20);
            textView.setLayoutParams(layoutParams);
            return new HistoryAdapter.OtherHolder(textView);
        } else if (viewType == typeLoading) {
            TextView textView = new TextView(parent.getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 30, 0, 30);
            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            return new HistoryAdapter.OtherHolder(textView);
        }
        return new HistoryAdapter.OtherHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == typeTime) {
            HistoryAdapter.OtherHolder timeHolder = (HistoryAdapter.OtherHolder) holder;
            ((TextView) timeHolder.itemView).setText((String) dataList.get(position));
        } else if (getItemViewType(position) == typeVideo) {
            Pair<ToWatch, VideoDetail> pair = (Pair<ToWatch, VideoDetail>) dataList.get(position);
            VideoDetail videoDetail = pair.getSecond();
            PlaylistVideoAdapter.VideoHolder videoHolder = (PlaylistVideoAdapter.VideoHolder) holder;
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
            videoHolder.itemView.setOnClickListener(v -> videoClickListener.onClick(videoDetail));
            videoHolder.itemView.setOnLongClickListener(v -> {
                optionClickListener.onClicked(pair);
                return true;
            });
            videoHolder.videoMore.setOnClickListener(v -> optionClickListener.onClicked(pair));
        } else if (getItemViewType(position) == typeLoading) {
            Boolean loadingFinished = (Boolean) dataList.get(position);
            HistoryAdapter.OtherHolder loadingHolder = (HistoryAdapter.OtherHolder) holder;
            if (loadingFinished)
                ((TextView) loadingHolder.itemView).setText("~ 没有更多了 ~");
            else ((TextView) loadingHolder.itemView).setText("加载中...");
        }
    }

    public void insert(List<Object> data) {
        dataList.remove(dataList.size() - 1);
        notifyItemRangeRemoved(dataList.size() - 1, 1);
        int count = dataList.size();
        dataList.addAll(data);
        notifyItemRangeInserted(count, data.size());
    }

    public void delete(List<Pair<ToWatch, VideoDetail>> pairs) {
        for (Pair<ToWatch, VideoDetail> pair : pairs) {
            int index = dataList.indexOf(pair);
            if (index >= 0) dataList.remove(index);
            notifyItemRemoved(index);
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
}
