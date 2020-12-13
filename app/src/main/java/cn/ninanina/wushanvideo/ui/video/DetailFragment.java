package cn.ninanina.wushanvideo.ui.video;

import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.common.util.CollectionUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.listener.DefaultDownloadClickListener;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.video.Tag;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.service.DownloadService;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.util.DialogManager;
import me.gujun.android.taggroup.TagGroup;

public class DetailFragment extends Fragment {

    @BindView(R.id.scroll)
    NestedScrollView scrollView;
    @BindView(R.id.content)
    LinearLayout content;
    @BindView(R.id.video_detail_title)
    TextView titleTextView;
    @BindView(R.id.video_detail_info)
    TextView infoTextView;
    @BindView(R.id.video_detail_like_button)
    ConstraintLayout likeButton;
    @BindView(R.id.video_detail_like_img)
    ImageView likeImg;
    @BindView(R.id.video_detail_like_num)
    TextView likeNum;
    @BindView(R.id.video_detail_collect_button)
    ConstraintLayout collectButton;
    @BindView(R.id.video_detail_collect_img)
    ImageView collectImg;
    @BindView(R.id.video_detail_collect_num)
    TextView collectNum;
    @BindView(R.id.video_detail_download_button)
    ConstraintLayout downloadButton;
    @BindView(R.id.video_detail_download_img)
    ImageView downloadImg;
    @BindView(R.id.video_detail_download_num)
    TextView downloadNum;
    @BindView(R.id.video_detail_dislike_button)
    ConstraintLayout dislikeButton;
    @BindView(R.id.video_detail_dislike_img)
    ImageView dislikeImg;
    @BindView(R.id.video_detail_dislike_num)
    TextView dislikeNum;
    @BindView(R.id.video_detail_tags)
    TagGroup videoTags;
    @BindView(R.id.detail_related_videos)
    RecyclerView relatedVideos;

    VideoDetail videoDetail;
    private boolean downloadEnabled = false;
    private boolean downloaded;
    private boolean disliked;
    private boolean liked;

    public final int page = 0;
    public final int size = 30;

    public DetailFragment(VideoDetail videoDetail) {
        super();
        this.videoDetail = videoDetail;
        this.videoDetail.setViewed(this.videoDetail.getViewed() / 100);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        StringBuilder strViewed = new StringBuilder();
        int viewed = videoDetail.getViewed();
        if (viewed >= 10000) {
            int wan = viewed / 10000;
            int qian = viewed % 10000 / 1000;
            strViewed.append(wan);
            if (qian != 0) strViewed.append('.').append(qian);
            strViewed.append("万");
        } else strViewed.append(viewed);
        strViewed.append("播放");
        infoTextView.setText(strViewed.toString());
        StringBuilder titleBuilder = new StringBuilder(videoDetail.getTitle());
        if (!StringUtils.isEmpty(videoDetail.getTitleZh()))
            titleBuilder.append("（机翻：").append(videoDetail.getTitleZh()).append("）");
        titleTextView.setText(titleBuilder.toString());
        if (!CollectionUtils.isEmpty(videoDetail.getTags())) {
            List<String> strTags = new ArrayList<>();
            for (Tag tag : videoDetail.getTags()) {
                if (!StringUtils.isEmpty(tag.getTagZh())) strTags.add(tag.getTagZh());
                else strTags.add(tag.getTag());
            }
            videoTags.setTags(strTags);
        }
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setSmoothScrollbarEnabled(true);
        relatedVideos.setLayoutManager(manager);
        relatedVideos.setNestedScrollingEnabled(false);
        relatedVideos.setHasFixedSize(true);
        VideoPresenter.getInstance().getRelatedVideos(this, videoDetail.getId());

        refreshCollect();
        refreshDownload();
        refreshLikeAndDislike();

        bindEvents();
    }

    private void bindEvents() {
        collectButton.setOnClickListener(collectListener);
        downloadButton.setOnClickListener(downloadListener);
        dislikeButton.setOnClickListener(dislikeListener);
        likeButton.setOnClickListener(likeListener);
    }

    View.OnClickListener collectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!WushanApp.loggedIn()) {
                DialogManager.getInstance().newLoginDialog(getContext()).show();
                return;
            }

            Dialog dialog = DialogManager.getInstance().newCollectDialog(getContext(), videoDetail);
            dialog.setOnDismissListener(dialog1 -> {
                for (int i = 1; i <= 10; i++) {
                    if (collectImg == null) return;
                    collectImg.postDelayed(() -> refreshCollect(), i * 100);
                }
            });
            dialog.show();
        }
    };

    View.OnClickListener downloadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!downloadEnabled) return;
            if (!WushanApp.loggedIn()) {
                DialogManager.getInstance().newLoginDialog(getContext()).show();
                return;
            }
            if (downloaded) {
                Toast.makeText(getContext(), "已经下载了", Toast.LENGTH_SHORT).show();
            } else {
                downloaded = true;
                new DefaultDownloadClickListener(MainActivity.getInstance().downloadService).onClick(videoDetail);
                downloadNum.postDelayed(() -> refreshDownload(), 200);
            }
        }
    };

    View.OnClickListener likeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!WushanApp.loggedIn()) {
                DialogManager.getInstance().newLoginDialog(getContext()).show();
                return;
            }
            VideoPresenter.getInstance().likeVideo(getContext(), videoDetail.getId());
            YoYo.with(Techniques.Bounce)
                    .duration(500)
                    .playOn(likeImg);
            if (liked) {
                likeImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.like_enabled));
                likeNum.setTextColor(getResources().getColor(R.color.tabColor, getContext().getTheme()));
                liked = false;
            } else {
                likeImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.like_clicked));
                likeNum.setTextColor(getResources().getColor(R.color.black, getContext().getTheme()));
                liked = true;
            }
        }
    };

    View.OnClickListener dislikeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!WushanApp.loggedIn()) {
                DialogManager.getInstance().newLoginDialog(getContext()).show();
                return;
            }
            VideoPresenter.getInstance().dislikeVideo(videoDetail.getId());
            YoYo.with(Techniques.Tada)
                    .duration(500)
                    .playOn(dislikeImg);
            if (disliked) {
                dislikeImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dislike_enabled));
                dislikeNum.setTextColor(getResources().getColor(R.color.tabColor, getContext().getTheme()));
                disliked = false;
            } else {
                dislikeImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dislike_clicked));
                dislikeNum.setTextColor(getResources().getColor(R.color.black, getContext().getTheme()));
                disliked = true;
            }
        }
    };

    public RecyclerView getRelatedRecyclerView() {
        return relatedVideos;
    }

    public void enableDownload() {
        if (downloadImg == null || downloadNum == null) return;
        if (downloaded) return;
        downloadEnabled = true;
        downloadImg.setImageResource(R.drawable.download_enabled);
        downloadNum.setTextColor(getContext().getColor(R.color.tabColor));
    }

    //刷新收藏状态
    public void refreshCollect() {
        if (collectImg == null || collectNum == null) return;
        if (!DataHolder.getInstance().collectedVideo(videoDetail.getId())) {
            collectImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang_enabled));
        } else {
            collectImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang_clicked));
        }
        if (videoDetail.getCollected() > 0)
            collectNum.setText(String.valueOf(videoDetail.getCollected()));
    }

    //刷新下载状态
    public void refreshDownload() {
        downloaded = WushanApp.getInstance().getDbHelper().downloaded(videoDetail.getId());
        if (downloaded) {
            downloadImg.setImageResource(R.drawable.download_clicked);
            downloadNum.setText("已下载");
            downloadNum.setTextColor(getContext().getColor(R.color.tabColor));
        } else {
            downloadImg.setImageResource(R.drawable.download_enabled);
            downloadNum.setText("下载");
        }
    }

    //刷新喜好
    public void refreshLikeAndDislike() {
        disliked = DataHolder.getInstance().dislikedVideo(videoDetail.getId());
        liked = DataHolder.getInstance().likedVideo(videoDetail.getId());
        if (disliked)
            dislikeImg.setImageResource(R.drawable.dislike_clicked);
        if (liked)
            likeImg.setImageResource(R.drawable.like_clicked);
        if (videoDetail.getLiked() > 0) likeNum.setText(String.valueOf(videoDetail.getLiked()));
        if (videoDetail.getDisliked() > 0)
            dislikeNum.setText(String.valueOf(videoDetail.getDisliked()));
    }

    public LinearLayout getContent() {
        return content;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
