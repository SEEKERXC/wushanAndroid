package cn.ninanina.wushanvideo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.model.bean.common.DownloadInfo;

public class DownloadingVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public DownloadingVideoAdapter(List<DownloadInfo> downloadInfoList) {
        this.downloadInfoList = downloadInfoList;
    }

    List<DownloadInfo> downloadInfoList;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DownloadingHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_downloading, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DownloadingHolder downloadingHolder = (DownloadingHolder) holder;
        DownloadInfo downloadInfo = downloadInfoList.get(position);
        downloadingHolder.cover.setAspectRatio(1.78f);
        downloadingHolder.cover.setImageURI(downloadInfo.getVideo().getCoverUrl());
        downloadingHolder.title.setText(downloadInfo.getFileName());
        downloadingHolder.speed.setText(downloadInfo.getSpeed());
        downloadingHolder.progress.setText(downloadInfo.getProgress());
        int width = downloadingHolder.unfinished.getMeasuredWidth();
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) downloadingHolder.finished.getLayoutParams();
        layoutParams.width = width * downloadInfo.getPercentage() / 100;
    }

    @Override
    public int getItemCount() {
        return downloadInfoList.size();
    }

    public void update(List<DownloadInfo> downloadInfoList) {
        this.downloadInfoList = downloadInfoList;
        notifyDataSetChanged();
    }

    static final class DownloadingHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cover)
        SimpleDraweeView cover;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.speed)
        TextView speed;
        @BindView(R.id.progress)
        TextView progress;
        @BindView(R.id.unfinished)
        View unfinished;
        @BindView(R.id.finished)
        View finished;

        public DownloadingHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
