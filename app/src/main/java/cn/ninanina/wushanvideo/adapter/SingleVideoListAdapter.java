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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.model.bean.video.Tag;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;

public class SingleVideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Object> dataList;
    private ItemClickListener listener;
    private OptionsClickListener optionsClickListener;

    public SingleVideoListAdapter(List<Object> dataList, ItemClickListener itemClickListener, OptionsClickListener optionsClickListener) {
        this.dataList = dataList;
        this.listener = itemClickListener;
        this.optionsClickListener = optionsClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoCardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_card_2, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (dataList.get(position) instanceof VideoDetail) {
            VideoCardHolder videoCardHolder = (VideoCardHolder) holder;
            VideoDetail videoDetail = (VideoDetail) dataList.get(position);
            videoCardHolder.cover.setAspectRatio(1.78f);
            videoCardHolder.cover.setImageURI(videoDetail.getCoverUrl());
            if (StringUtils.isEmpty(videoDetail.getTitleZh()))
                videoCardHolder.videoTitle.setText(videoDetail.getTitle());
            else videoCardHolder.videoTitle.setText(videoDetail.getTitleZh());
            int viewed = videoDetail.getViewed();
            viewed = viewed / 100;
            StringBuilder strViewed = new StringBuilder();
            if (viewed >= 10000) {
                int wan = viewed / 10000;
                int qian = viewed % 10000 / 1000;
                strViewed.append(wan);
                if (qian != 0) strViewed.append('.').append(qian);
                strViewed.append("万");
            } else strViewed.append(viewed);
            videoCardHolder.videoViews.setText(strViewed.toString());
            String duration = videoDetail.getDuration().replace(" ", "\0").replace("min", "分钟").replace("h", "小时").replace("sec", "秒");
            videoCardHolder.videoDuration.setText(duration);
            StringBuilder strTag = new StringBuilder();
            if (VideoListAdapter.isSrcValid(videoDetail.getSrc())) {
                videoCardHolder.label1.setText("不限次");
                videoCardHolder.label1.setVisibility(View.VISIBLE);
                videoCardHolder.label1.setPadding(3, 0, 0, 3);
            }
            for (Tag tag : videoDetail.getTags()) {
                if (!StringUtils.isEmpty(tag.getTagZh())) {
                    strTag.append(tag.getTagZh()).append("·");
                }
                if (strTag.length() >= 12)
                    break;
            }
            if (strTag.toString().isEmpty() && videoDetail.getTags().size() > 0)
                strTag.append(videoDetail.getTags().get(0).getTag());
            String sTag = strTag.toString();
            if (sTag.endsWith("·")) sTag = sTag.substring(0, sTag.length() - 1);
            videoCardHolder.label2.setText(sTag);
            videoCardHolder.videoCard.setOnClickListener(v -> listener.onItemClicked((VideoDetail) dataList.get(holder.getLayoutPosition())));
            videoCardHolder.videoMore.setOnClickListener(v -> optionsClickListener.onOptionsClicked((VideoDetail) dataList.get(holder.getLayoutPosition())));
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public static final class VideoCardHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.video_cover)
        SimpleDraweeView cover;
        @BindView(R.id.video_title)
        TextView videoTitle;
        @BindView(R.id.video_views)
        TextView videoViews;
        @BindView(R.id.video_duration)
        TextView videoDuration;
        @BindView(R.id.video_label1)
        TextView label1;
        @BindView(R.id.video_label2)
        TextView label2;
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

    public interface ItemClickListener {
        void onItemClicked(VideoDetail videoDetail);
    }

    public interface OptionsClickListener {
        void onOptionsClicked(VideoDetail videoDetail);
    }
}