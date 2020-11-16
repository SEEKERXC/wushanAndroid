package cn.ninanina.wushanvideo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.model.bean.video.Tag;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import me.gujun.android.taggroup.TagGroup;

//视频列表，每四个视频放一个广告
public class VideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Object> dataList;
    private ItemClickListener listener;

    public VideoListAdapter(List<Object> dataList, ItemClickListener listener) {
        this.dataList = dataList;
        this.listener = listener;
    }

    private int TYPE_AD = 0;
    private int TYPE_CONTENT = 1;
    public static int ITEMS_PER_AD = 4;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_AD) {
            return new AdViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner_ad, parent, false));
        } else if (viewType == TYPE_CONTENT) {
            return new VideoCardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_card, parent, false));
        }
        return new VideoCardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position % ITEMS_PER_AD != 0) {
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
                String duration = videoDetail.getDuration().replace("min", "分钟").replace("h", "小时").replace("sec", "秒");
                videoCardHolder.videoDuration.setText(duration);
                List<String> strTags = new ArrayList<>();
                if (!StringUtils.isEmpty(videoDetail.getSrc()) && isSrcValid(videoDetail.getSrc()))
                    strTags.add("免广告");
                for (Tag tag : videoDetail.getTags()) {
                    if (!StringUtils.isEmpty(tag.getTagZh()) && !strTags.contains(tag.getTagZh())) {
                        if (tag.getTagZh().length() <= 4)
                            strTags.add(tag.getTagZh());
                    }
                    if (strTags.size() >= 4) break;
                }
                if (strTags.isEmpty() && videoDetail.getTags().size() > 0)
                    strTags.add(videoDetail.getTags().get(0).getTag());
                videoCardHolder.videoTags.setTags(strTags);

                videoCardHolder.cover.setOnClickListener(v -> listener.onItemClicked((VideoDetail) dataList.get(holder.getLayoutPosition())));
                videoCardHolder.videoTitle.setOnClickListener(v -> listener.onItemClicked((VideoDetail) dataList.get(holder.getLayoutPosition())));
            }
        } else {
            if (dataList.get(position) instanceof AdView) {
                AdViewHolder bannerHolder = (AdViewHolder) holder;
                AdView adView = (AdView) dataList.get(position);
                ViewGroup adCardView = (ViewGroup) bannerHolder.itemView;
                // The AdViewHolder recycled by the RecyclerView may be a different
                // instance than the one used previously for this position. Clear the
                // AdViewHolder of any subviews in case it has a different
                // AdView associated with it, and make sure the AdView for this position doesn't
                // already have a parent of a different recycled AdViewHolder.
                if (adCardView.getChildCount() > 0) {
                    adCardView.removeAllViews();
                }
                if (adView.getParent() != null) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }

                // Add the banner ad to the ad view.
                adCardView.addView(adView);
                adView.loadAd(new AdRequest.Builder().build());
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position % ITEMS_PER_AD != 0) {
            return TYPE_CONTENT;
        } else {
            return TYPE_AD;
        }
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
        @BindView(R.id.video_item_tags)
        TagGroup videoTags;
        @BindView(R.id.video_close)
        ImageButton videoDislike;

        private VideoCardHolder(View itemView) {
            super(itemView);
            if (!(itemView instanceof AdView))
                ButterKnife.bind(this, itemView);
        }
    }

    static final class AdViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.video_list_ad)
        AdView adView;

        AdViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface ItemClickListener {
        void onItemClicked(VideoDetail videoDetail);
    }

    public void append(List<Object> newData) {
        int index = dataList.size();
        dataList.addAll(newData);
        notifyItemRangeInserted(index, newData.size());
    }

    public void insertToStart(List<Object> newData) {
        dataList.addAll(0, newData);
        notifyItemRangeInserted(0, newData.size());
    }

    //规定时长小于2.5小时的src为有效
    private boolean isSrcValid(String src) {
        long currentSeconds = System.currentTimeMillis() / 1000;
        long urlSeconds = Long.parseLong(src.substring(src.indexOf("?e=") + 3, src.indexOf("&h=")));
        long interval = 3600 * 5 / 2; //规定视频失效时间为2.5小时
        return currentSeconds - urlSeconds < interval;
    }
}
