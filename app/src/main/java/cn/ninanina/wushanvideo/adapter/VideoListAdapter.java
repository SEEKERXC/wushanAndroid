package cn.ninanina.wushanvideo.adapter;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.holder.AdHolder;
import cn.ninanina.wushanvideo.adapter.listener.VideoClickListener;
import cn.ninanina.wushanvideo.model.bean.video.Tag;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.util.CommonUtils;

public class VideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Object> dataList;
    private VideoClickListener listener;
    private VideoClickListener optionsClickListener;

    private static final int TYPE_VIDEO = 0;
    private static final int TYPE_AD = 1;

    private int imageHeight;
    private int imageWidth;

    public VideoListAdapter(List<Object> dataList, VideoClickListener itemClickListener, VideoClickListener optionsClickListener) {
        this.dataList = dataList;
        this.listener = itemClickListener;
        this.optionsClickListener = optionsClickListener;
        imageWidth = WushanApp.getConstants().getInt("image_width", 0);
        imageHeight = WushanApp.getConstants().getInt("image_height", 0);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_VIDEO)
            return new VideoCardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_card, parent, false));
        else if (viewType == TYPE_AD)
            return new AdHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ad_unified, parent, false));
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_VIDEO) {
            VideoCardHolder videoCardHolder = (VideoCardHolder) holder;
            VideoDetail videoDetail = (VideoDetail) dataList.get(position);
            videoCardHolder.cover.setAspectRatio(1.78f);
            videoCardHolder.cover.setImageURI(videoDetail.getCoverUrl());
            if (imageHeight == 0 || imageWidth == 0) {
                imageHeight = videoCardHolder.cover.getMeasuredHeight();
                imageWidth = videoCardHolder.cover.getMeasuredWidth();
                WushanApp.getConstants().edit().putInt("image_width", imageWidth).putInt("image_height", imageHeight).apply();
            }
            if (StringUtils.isEmpty(videoDetail.getTitleZh()))
                videoCardHolder.videoTitle.setText(videoDetail.getTitle());
            else videoCardHolder.videoTitle.setText(videoDetail.getTitleZh());
            videoCardHolder.videoViews.setText(CommonUtils.getViewsString(videoDetail.getViewed() / 100));
            videoCardHolder.videoDuration.setText(CommonUtils.getDurationString(videoDetail.getDuration()));
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
            videoCardHolder.itemView.setOnClickListener(v -> listener.onClick((VideoDetail) dataList.get(holder.getLayoutPosition())));
            videoCardHolder.videoMore.setOnClickListener(v -> optionsClickListener.onClick((VideoDetail) dataList.get(holder.getLayoutPosition())));
            videoCardHolder.itemView.setOnLongClickListener(v -> {
                optionsClickListener.onClick((VideoDetail) dataList.get(holder.getLayoutPosition()));
                return true;
            });
        } else if (getItemViewType(position) == TYPE_AD) {
            populateNativeAdView((UnifiedNativeAd) dataList.get(position), ((AdHolder) holder).getAdView());
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
        Object o = dataList.get(position);
        if (o instanceof VideoDetail) return TYPE_VIDEO;
        else if (o instanceof UnifiedNativeAd) return TYPE_AD;
        return super.getItemViewType(position);
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
        @BindView(R.id.tag_group)
        ConstraintLayout tagGroup;
        @BindView(R.id.video_label)
        TextView label;
        @BindView(R.id.video_more)
        FrameLayout videoMore;
        @BindView(R.id.video_card)
        CardView videoCard;

        private VideoCardHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
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

    private void populateNativeAdView(UnifiedNativeAd nativeAd,
                                      UnifiedNativeAdView adView) {
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        adView.getHeadlineView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int headLineCount = ((TextView) adView.getHeadlineView()).getLineCount();
                if (headLineCount <= 1) ((TextView) adView.getBodyView()).setMaxLines(2);
                else ((TextView) adView.getBodyView()).setMaxLines(1);
                adView.getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });

        adView.setNativeAd(nativeAd);

        if (imageHeight != 0 && imageWidth != 0) {
            ViewGroup.LayoutParams layoutParams = adView.getMediaView().getLayoutParams();
            layoutParams.width = imageWidth;
            layoutParams.height = imageHeight;
        }
    }

}
