package cn.ninanina.wushanvideo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import cn.ninanina.wushanvideo.util.DBHelper;
import cn.ninanina.wushanvideo.util.FileUtil;
import cn.ninanina.wushanvideo.util.TimeUtil;

public class DownloadedVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<VideoDetail> dataList;
    private VideoClickListener listener;
    private VideoClickListener optionsClickListener;

    public DownloadedVideoAdapter(List<VideoDetail> dataList, VideoClickListener itemClickListener, VideoClickListener optionsClickListener) {
        this.dataList = dataList;
        this.listener = itemClickListener;
        this.optionsClickListener = optionsClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DownloadedHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_downloaded, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DownloadedHolder downloadedHolder = (DownloadedHolder) holder;
        downloadedHolder.cover.setAspectRatio(1.78f);
        VideoDetail videoDetail = dataList.get(position);
        if (videoDetail.getId() != null) {
            downloadedHolder.cover.setImageURI(videoDetail.getCoverUrl());
            DBHelper dbHelper = WushanApp.getInstance().getDbHelper();
            downloadedHolder.videoTitle.setText(dbHelper.getNameById(videoDetail.getId()));
            String duration = videoDetail.getDuration().replace(" ", "\0").replace("min", "分钟").replace("h", "小时").replace("sec", "秒");
            downloadedHolder.videoDuration.setText(duration);
            downloadedHolder.info.setText(FileUtil.getSize(videoDetail.getSize()) + "  |  " + TimeUtil.getFullTime(videoDetail.getUpdateTime()));
            if (WushanApp.loggedIn()) {
                int viewCount = DataHolder.getInstance().viewedCount(videoDetail.getId());
                if (viewCount <= 0) downloadedHolder.watchInfo.setText("未观看");
                else downloadedHolder.watchInfo.setText("看过" + viewCount + "次");
            }
            downloadedHolder.itemView.setOnClickListener(v -> listener.onClick(dataList.get(holder.getLayoutPosition())));
            downloadedHolder.videoMore.setOnClickListener(v -> optionsClickListener.onClick(dataList.get(holder.getLayoutPosition())));
            downloadedHolder.itemView.setOnLongClickListener(v -> {
                optionsClickListener.onClick(dataList.get(holder.getLayoutPosition()));
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void updateOne(long videoId) {
        for (int i = 0; i < dataList.size(); i++) {
            VideoDetail videoDetail = dataList.get(i);
            if (videoDetail.getId() == videoId) {
                notifyItemChanged(i);
            }
        }
    }

    public void deleteOne(long videoId) {
        int toDelete = -1;
        for (int i = 0; i < dataList.size(); i++) {
            VideoDetail videoDetail = dataList.get(i);
            if (videoDetail.getId() == videoId) {
                toDelete = i;
            }
        }
        if (toDelete != -1) {
            dataList.remove(toDelete);
            notifyItemRemoved(toDelete);
        }
    }

    static final class DownloadedHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.video_cover)
        SimpleDraweeView cover;
        @BindView(R.id.video_title)
        TextView videoTitle;
        @BindView(R.id.info)
        TextView info;
        @BindView(R.id.watch_info)
        TextView watchInfo;
        @BindView(R.id.video_duration)
        TextView videoDuration;
        @BindView(R.id.video_action)
        FrameLayout videoMore;

        private DownloadedHolder(View itemView) {
            super(itemView);
            if (!(itemView instanceof AdView))
                ButterKnife.bind(this, itemView);
        }
    }
}
