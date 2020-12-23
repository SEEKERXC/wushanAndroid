package cn.ninanina.wushanvideo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.common.util.CollectionUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.listener.VideoClickListener;
import cn.ninanina.wushanvideo.model.bean.video.Tag;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.util.CommonUtils;

public class SingleVideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Object> dataList;
    private VideoClickListener listener;
    private VideoClickListener optionsClickListener;

    private static final int TYPE_VIDEO = 0;
    private static final int TYPE_AD = 1;

    private int imageHeight = 0;
    private int imageWidth = 0;

    public SingleVideoListAdapter(List<Object> dataList, VideoClickListener itemClickListener, VideoClickListener optionsClickListener) {
        this.dataList = dataList;
        this.listener = itemClickListener;
        this.optionsClickListener = optionsClickListener;
        imageWidth = WushanApp.getConstants().getInt("small_image_width", 0);
        imageHeight = WushanApp.getConstants().getInt("small_image_height", 0);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_VIDEO) {
            return new VideoCardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_card_2, parent, false));
        } else if (viewType == TYPE_AD) {
            return new AdHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_small_ad, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_VIDEO) {
            VideoCardHolder videoCardHolder = (VideoCardHolder) holder;
            VideoDetail videoDetail = (VideoDetail) dataList.get(position);
            videoCardHolder.cover.setAspectRatio(1.78f);
            videoCardHolder.cover.setImageURI(videoDetail.getCoverUrl());
            if (imageHeight <= 0 || imageWidth <= 0) {
                videoCardHolder.cover.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        imageHeight = videoCardHolder.cover.getMeasuredHeight();
                        imageWidth = videoCardHolder.cover.getMeasuredWidth();
                        WushanApp.getConstants().edit().putInt("small_image_width", imageWidth).putInt("small_image_height", imageHeight).apply();
                        videoCardHolder.cover.getViewTreeObserver().removeOnPreDrawListener(this);
                        return false;
                    }
                });

            }
            if (StringUtils.isEmpty(videoDetail.getTitleZh()))
                videoCardHolder.videoTitle.setText(videoDetail.getTitle());
            else videoCardHolder.videoTitle.setText(videoDetail.getTitleZh());
            videoCardHolder.videoViews.setText(CommonUtils.getViewsString(videoDetail.getViewed() / 100));
            videoCardHolder.videoDuration.setText(CommonUtils.getDurationString(videoDetail.getDuration()));
            if (!CollectionUtils.isEmpty(videoDetail.getTags())) {
                StringBuilder strTag = new StringBuilder();
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
                videoCardHolder.label.setText(sTag);
            }
            videoCardHolder.itemView.setOnClickListener(v -> listener.onClick((VideoDetail) dataList.get(holder.getLayoutPosition())));
            videoCardHolder.videoMore.setOnClickListener(v -> optionsClickListener.onClick((VideoDetail) dataList.get(holder.getLayoutPosition())));
            videoCardHolder.itemView.setOnLongClickListener(v -> {
                optionsClickListener.onClick((VideoDetail) dataList.get(holder.getLayoutPosition()));
                return true;
            });
        } else if (getItemViewType(position) == TYPE_AD) {
            AdHolder adHolder = (AdHolder) holder;
            UnifiedNativeAd ad = (UnifiedNativeAd) dataList.get(position);
            adHolder.adView.setNativeAd(ad);
            adHolder.mediaView.setMediaContent(ad.getMediaContent());
            adHolder.headline.setText(ad.getHeadline());
            adHolder.body.setText(ad.getBody());
            if (imageHeight > 0 && imageWidth > 0) {
                ViewGroup.LayoutParams layoutParams = adHolder.mediaView.getLayoutParams();
                layoutParams.width = imageWidth;
                layoutParams.height = imageHeight;
                adHolder.mediaView.setLayoutParams(layoutParams);
            }
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

    @Override
    public int getItemViewType(int position) {
        if (dataList.get(position) instanceof VideoDetail) return TYPE_VIDEO;
        else if (dataList.get(position) instanceof UnifiedNativeAd) return TYPE_AD;
        return super.getItemViewType(position);
    }

    public void insertToStart(List<Object> newData) {
        dataList.addAll(0, newData);
        notifyItemRangeInserted(0, newData.size());
    }

    public void insert(List<Object> newData) {
        int index = dataList.size();
        dataList.addAll(newData);
        notifyItemRangeInserted(index, newData.size());
    }

    public void insert(VideoDetail videoDetail) {
        dataList.add(videoDetail);
        notifyItemInserted(dataList.size() - 1);
    }

    public void delete(VideoDetail videoDetail) {
        int index = dataList.indexOf(videoDetail);
        if (index >= 0) {
            dataList.remove(index);
            notifyItemRemoved(index);
        }
    }

    static final class VideoCardHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.video_cover)
        SimpleDraweeView cover;
        @BindView(R.id.video_title)
        TextView videoTitle;
        @BindView(R.id.video_views)
        TextView videoViews;
        @BindView(R.id.video_duration)
        TextView videoDuration;
        @BindView(R.id.video_label)
        TextView label;
        @BindView(R.id.video_action)
        FrameLayout videoMore;

        private VideoCardHolder(View itemView) {
            super(itemView);
            if (!(itemView instanceof AdView))
                ButterKnife.bind(this, itemView);
        }
    }

    static final class AdHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ad_view)
        UnifiedNativeAdView adView;
        @BindView(R.id.media_view)
        MediaView mediaView;
        @BindView(R.id.ad_headline)
        TextView headline;
        @BindView(R.id.ad_body)
        TextView body;

        public AdHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            adView.setMediaView(mediaView);
            adView.setHeadlineView(headline);
            adView.setBodyView(body);
        }
    }
}
