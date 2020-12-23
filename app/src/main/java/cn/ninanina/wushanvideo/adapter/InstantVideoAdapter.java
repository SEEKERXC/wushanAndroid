package cn.ninanina.wushanvideo.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerAdapter;
import com.youth.banner.indicator.CircleIndicator;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.listener.DownloadClickListener;
import cn.ninanina.wushanvideo.adapter.listener.VideoClickListener;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.ui.me.LoginActivity;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;
import cn.ninanina.wushanvideo.util.CommonUtils;
import cn.ninanina.wushanvideo.util.DialogManager;
import cn.ninanina.wushanvideo.util.LayoutUtil;
import cn.ninanina.wushanvideo.util.PlayTimeManager;
import cn.ninanina.wushanvideo.util.TimeUtil;
import cn.ninanina.wushanvideo.util.ToastUtil;

public class InstantVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AppCompatActivity activity;
    public List<Object> dataList;

    private VideoClickListener videoClickListener;
    private DownloadClickListener downloadListener;

    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_AD = 1;
    public static final int TYPE_BANNER = 2;


    public InstantVideoAdapter(AppCompatActivity activity, List<Object> dataList) {
        this.activity = activity;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_VIDEO) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_instant, parent, false);
            return new InstantVideoHolder(view);
        } else if (viewType == TYPE_BANNER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
            return new BannerHolder(view);
        } else if (viewType == TYPE_AD) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_instant_ad, parent, false);
            return new UnifiedAdHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_VIDEO:
                VideoDetail videoDetail = (VideoDetail) dataList.get(position);
                InstantVideoHolder videoHolder = (InstantVideoHolder) holder;
                if (videoHolder.itemView.getTag() != null && (int) videoHolder.itemView.getTag() >= 0)
                    return;
                videoHolder.itemView.setTag(position);

                videoHolder.cover.setAspectRatio(1.78f);
                videoHolder.cover.setImageURI(videoDetail.getCoverUrl());
                videoHolder.title.setText(StringUtils.isEmpty(videoDetail.getTitleZh()) ? videoDetail.getTitle() : videoDetail.getTitleZh());

                SimpleExoPlayer player = new SimpleExoPlayer.Builder(activity).build();
                if (CommonUtils.isSrcValid(videoDetail.getSrc())) {
                    MediaItem mediaItem = MediaItem.fromUri(videoDetail.getSrc());
                    player.setMediaItem(mediaItem);
                    player.prepare();
                } else {
                    VideoPresenter.getInstance().getSrcForInstant(player, videoDetail);
                }
                Runnable closeControllerTask = () -> {
                    if (videoHolder.controller.getVisibility() == View.VISIBLE) {
                        videoHolder.controller.setVisibility(View.INVISIBLE);
                        videoHolder.shadow.setVisibility(View.INVISIBLE);
                    }
                };
                player.addListener(new Player.EventListener() {
                    private ScheduledExecutorService executorService;

                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        if (!isPlaying) {
                            videoHolder.controller.setVisibility(View.VISIBLE);
                            videoHolder.shadow.setVisibility(View.VISIBLE);
                            videoHolder.playIcon.setImageResource(R.drawable.play_white);
                            videoHolder.controller.postDelayed(closeControllerTask, 3000);
                            PlayTimeManager.stopTiming();
                            if (executorService != null) executorService.shutdownNow();
                        } else {
                            PlayTimeManager.startTiming(videoDetail.getId());
                            videoHolder.cover.setVisibility(View.INVISIBLE);
                            videoHolder.playIcon.setImageResource(R.drawable.pause);
                            long duration = player.getDuration();
                            executorService = new ScheduledThreadPoolExecutor(1);
                            executorService.scheduleAtFixedRate(() -> activity.runOnUiThread(() -> {
                                ConstraintLayout.LayoutParams playedParams = (ConstraintLayout.LayoutParams) videoHolder.played.getLayoutParams();
                                ConstraintLayout.LayoutParams indicatorParams = (ConstraintLayout.LayoutParams) videoHolder.indicator.getLayoutParams();
                                int fullWidth = videoHolder.notPlayed.getMeasuredWidth();
                                long position = player.getCurrentPosition();
                                videoHolder.timeText.setText(TimeUtil.getDuration(position) + "/" + TimeUtil.getDuration(duration));
                                if (position > 1000) {
                                    playedParams.height = LayoutUtil.dip2px(activity, 3);
                                    int w = Double.valueOf((double) position / (double) duration * (double) fullWidth).intValue();
                                    if (w > 0) playedParams.width = w;
                                    videoHolder.played.setLayoutParams(playedParams);
                                    indicatorParams.leftMargin = w;
                                }
                            }), 500, 500, TimeUnit.MILLISECONDS);
                        }
                    }

                    @Override
                    public void onPlaybackStateChanged(int state) {
                        if (state == Player.STATE_READY) {
                            long duration = player.getDuration();
                            videoHolder.shadow.setVisibility(View.VISIBLE);
                            videoHolder.controller.setVisibility(View.VISIBLE);
                            videoHolder.controller.postDelayed(closeControllerTask, 3000);
                            videoHolder.playButton.setOnClickListener(v -> {
                                if (player.isPlaying()) player.stop();
                                else {
                                    if (PlayTimeManager.getTodayWatchTime() > 60 * 60) {
                                        DialogManager.getInstance().newWatchPromptDialog(activity, videoDetail, new VideoClickListener() {
                                            @Override
                                            public void onClick(VideoDetail videoDetail) {
                                            }

                                            @Override
                                            public void playVideo(VideoDetail videoDetail) {
                                                player.prepare();
                                                player.play();
                                            }
                                        }).show();
                                        return;
                                    }
                                    player.prepare();
                                    player.play();
                                }
                            });
                            videoHolder.timeText.setText("00:00/" + TimeUtil.getDuration(duration));
                            videoHolder.progressBar.setOnTouchListener((v, event) -> {
                                float x = event.getX() - 10;
                                if (x > 10) {
                                    int fullWidth = videoHolder.notPlayed.getMeasuredWidth();
                                    player.seekTo(Double.valueOf(x / (float) fullWidth * duration).longValue());
                                }
                                return true;
                            });
                            videoHolder.forwardButton.setOnClickListener(v -> videoClickListener.onClick(videoDetail));

                        } else if (state == Player.STATE_ENDED) {
                            player.seekTo(0);
                        }
                    }

                });
                videoHolder.player.setPlayer(player);
                videoHolder.player.setUseController(false);
                videoHolder.player.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                videoHolder.itemView.setOnClickListener(v -> {
                    if (videoHolder.controller.getVisibility() == View.VISIBLE) {
                        videoHolder.controller.setVisibility(View.INVISIBLE);
                        videoHolder.shadow.setVisibility(View.INVISIBLE);
                        videoHolder.controller.removeCallbacks(closeControllerTask);
                    } else {
                        videoHolder.controller.setVisibility(View.VISIBLE);
                        videoHolder.shadow.setVisibility(View.VISIBLE);
                        videoHolder.controller.postDelayed(closeControllerTask, 3000);
                    }
                });
                videoHolder.middle.setOnClickListener(v -> videoClickListener.onClick(videoDetail));
                videoHolder.collectButton.setOnClickListener(v -> {
                    if (!WushanApp.loggedIn()) {
                        DialogManager.getInstance().newLoginDialog(activity).show();
                        return;
                    }
                    DialogManager.getInstance().newCollectDialog(activity, videoDetail).show();
                });
                if (videoDetail.getCollected() > 0)
                    videoHolder.collectText.setText(String.valueOf(videoDetail.getCollected()));
                videoHolder.downloadButton.setOnClickListener(v -> {
                    if (!WushanApp.loggedIn()) {
                        DialogManager.getInstance().newLoginDialog(activity).show();
                        return;
                    }
                    downloadListener.onClick(videoDetail);
                });
                if (videoDetail.getDownloaded() > 0)
                    videoHolder.downloadText.setText(String.valueOf(videoDetail.getDownloaded()));
                final boolean[] liked = {DataHolder.getInstance().likedVideo(videoDetail.getId())};
                boolean disliked = DataHolder.getInstance().dislikedVideo(videoDetail.getId());
                if (liked[0])
                    videoHolder.likeImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.like_clicked));
                if (disliked)
                    videoHolder.dislikeImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.dislike_clicked));
                videoHolder.likeButton.setOnClickListener(v -> {
                    if (!WushanApp.loggedIn()) {
                        DialogManager.getInstance().newLoginDialog(activity).show();
                        return;
                    }
                    liked[0] = !liked[0];
                    VideoPresenter.getInstance().likeVideo(activity, videoDetail);
                    if (liked[0])
                        videoHolder.likeImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.like_clicked));
                    else
                        videoHolder.likeImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.like));
                });
                if (videoDetail.getLiked() > 0)
                    videoHolder.likeText.setText(String.valueOf(videoDetail.getLiked()));
                videoHolder.dislikeButton.setOnClickListener(v -> {
                    if (!WushanApp.loggedIn()) {
                        DialogManager.getInstance().newLoginDialog(activity).show();
                        return;
                    }
                    player.stop();
                    player.release();
                    VideoPresenter.getInstance().dislikeVideo(activity, videoDetail);
                });
                if (videoDetail.getDisliked() > 0)
                    videoHolder.dislikeText.setText(String.valueOf(videoDetail.getDisliked()));
                break;
            case TYPE_AD:
                UnifiedAdHolder adHolder = ((UnifiedAdHolder) holder);
                UnifiedNativeAd ad = (UnifiedNativeAd) dataList.get(position);
                adHolder.adView.setNativeAd(ad);
                adHolder.adBody.setText(ad.getBody());
                adHolder.mediaView.setMediaContent(ad.getMediaContent());
                adHolder.headline.setText(ad.getHeadline());
                break;
            case TYPE_BANNER:
                BannerHolder bannerHolder = (BannerHolder) holder;
                bannerHolder.banner.addBannerLifecycleObserver(activity)
                        .setAdapter(new BannerAdAdapter((List<UnifiedNativeAd>) dataList.get(position)))
                        .setIndicator(new CircleIndicator(activity));
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
        else if (o instanceof List) return TYPE_BANNER;
        return super.getItemViewType(position);
    }

    public void insert(List<Object> data) {
        int count = dataList.size();
        dataList.addAll(data);
        notifyItemRangeInserted(count, data.size());
    }

    public List<Object> getDataList() {
        return dataList;
    }

    public InstantVideoAdapter setDownloadListener(DownloadClickListener downloadListener) {
        this.downloadListener = downloadListener;
        return this;
    }

    public InstantVideoAdapter setVideoClickListener(VideoClickListener videoClickListener) {
        this.videoClickListener = videoClickListener;
        return this;
    }

    static final class InstantVideoHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cover)
        SimpleDraweeView cover;
        @BindView(R.id.player)
        StyledPlayerView player;
        @BindView(R.id.middle)
        LinearLayout middle;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.collect_button)
        ConstraintLayout collectButton;
        @BindView(R.id.collect_text)
        TextView collectText;
        @BindView(R.id.download_button)
        ConstraintLayout downloadButton;
        @BindView(R.id.download_text)
        TextView downloadText;
        @BindView(R.id.like_button)
        ConstraintLayout likeButton;
        @BindView(R.id.like_img)
        ImageView likeImg;
        @BindView(R.id.like_text)
        TextView likeText;
        @BindView(R.id.dislike_button)
        ConstraintLayout dislikeButton;
        @BindView(R.id.dislike_img)
        ImageView dislikeImg;
        @BindView(R.id.dislike_text)
        TextView dislikeText;
        @BindView(R.id.shadow)
        View shadow;
        @BindView(R.id.bottom_controller)
        LinearLayout controller;
        @BindView(R.id.play_button)
        FrameLayout playButton;
        @BindView(R.id.play_icon)
        ImageView playIcon;
        @BindView(R.id.progress_bar)
        ConstraintLayout progressBar;
        @BindView(R.id.progress_not_played)
        View notPlayed;
        @BindView(R.id.progress_played)
        View played;
        @BindView(R.id.indicator)
        ImageView indicator;
        @BindView(R.id.time)
        TextView timeText;
        @BindView(R.id.full_button)
        ImageView forwardButton; //这里不做全屏，改为指向详情页的按钮

        public InstantVideoHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setKeepScreenOn(true);
            forwardButton.setImageResource(R.drawable.forward_white);
        }
    }

    static class BannerHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.banner)
        Banner<UnifiedNativeAd, BannerAdAdapter> banner;

        public BannerHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class BannerAdAdapter extends BannerAdapter<UnifiedNativeAd, BannerAdAdapter.AdViewHolder> {
        public BannerAdAdapter(List<UnifiedNativeAd> data) {
            super(data);
        }

        @Override
        public AdViewHolder onCreateHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner_ad, parent, false);
            return new AdViewHolder(view);
        }

        @Override
        public void onBindView(AdViewHolder holder, UnifiedNativeAd data, int position, int size) {
            holder.adView.setNativeAd(data);
            holder.headline.setText(data.getHeadline());
        }

        static class AdViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.ad_view)
            UnifiedNativeAdView adView;
            @BindView(R.id.ad_media)
            MediaView mediaView;
            @BindView(R.id.ad_headline)
            TextView headline;

            public AdViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                adView.setMediaView(mediaView);
                adView.setHeadlineView(headline);
            }
        }
    }

    static class UnifiedAdHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ad_view)
        UnifiedNativeAdView adView;
        @BindView(R.id.ad_media)
        MediaView mediaView;
        @BindView(R.id.ad_headline)
        TextView headline;
        @BindView(R.id.ad_body)
        TextView adBody;

        public UnifiedAdHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            adView.setMediaView(mediaView);
            adView.setHeadlineView(headline);
            adView.setBodyView(adBody);
        }
    }
}
