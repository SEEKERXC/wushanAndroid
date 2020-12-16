package cn.ninanina.wushanvideo.ui.video;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.listener.DefaultDownloadClickListener;
import cn.ninanina.wushanvideo.adapter.listener.TagClickListener;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.video.Tag;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.util.DialogManager;
import me.gujun.android.taggroup.TagGroup;

public class DetailFragment extends Fragment {

    @BindView(R.id.scroll)
    NestedScrollView scrollView;
    @BindView(R.id.content)
    public LinearLayout content;
    @BindView(R.id.video_detail_title)
    TextView titleTextView;
    @BindView(R.id.video_detail_info)
    public TextView infoTextView;
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

    public VideoDetail videoDetail;
    private boolean downloadEnabled = false;
    private boolean downloaded;
    private boolean disliked;
    private boolean liked;

    public int page = 0;
    public final int size = 10;//分三次加载，共加载30个相关视频

    public Handler handler;
    public static final int downloading = 0;//正在下载
    public static final int downloadFinish = 1; //下载完成
    public static final int refreshData = 2;//刷新收藏数、喜欢和不喜欢数

    public DetailFragment(VideoDetail v) {
        super();
        videoDetail = v;
        videoDetail.setViewed(videoDetail.getViewed() / 100);
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
        Collections.sort(videoDetail.getTags(), (o1, o2) -> o2.getVideoCount() - o1.getVideoCount());
        if (!CollectionUtils.isEmpty(videoDetail.getTags())) {
            List<String> strTags = new ArrayList<>();
            for (Tag tag : videoDetail.getTags()) {
                if (strTags.contains(tag.getTagZh()) || strTags.contains(tag.getTag())) continue;
                if (!StringUtils.isEmpty(tag.getTagZh())) strTags.add(tag.getTagZh());
                else strTags.add(tag.getTag());
            }
            videoTags.setTags(strTags);
            videoTags.setOnTagClickListener(tag -> {
                for (Tag tag1 : videoDetail.getTags()) {
                    if ((!StringUtils.isEmpty(tag1.getTagZh()) && tag1.getTagZh().equals(tag)) || tag1.getTag().equals(tag)) {
                        new TagClickListener(getContext()).onTagClicked(tag1);
                    }
                }
            });
        }
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setSmoothScrollbarEnabled(true);
        relatedVideos.setLayoutManager(manager);
        relatedVideos.setNestedScrollingEnabled(false);
        VideoPresenter.getInstance().getRelatedVideos(this, videoDetail.getId(), true);
        page++;
        VideoPresenter.getInstance().getRelatedVideos(this, videoDetail.getId(), false);
        page++;
        VideoPresenter.getInstance().getRelatedVideos(this, videoDetail.getId(), false);

        refreshCollect();
        refreshDownload();
        refreshLikeAndDislike();

        bindEvents();
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (downloadNum == null) return;
                if (msg.what == downloading) {
                    downloadNum.setText("正在下载");
                } else if (msg.what == downloadFinish) {
                    downloadNum.setText("下载完成");
                } else if (msg.what == refreshData) {
                    refreshCollect();
                    refreshLikeAndDislike();
                }
                super.handleMessage(msg);
            }
        };
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

            Dialog dialog = DialogManager.getInstance().newCollectDialog(getActivity(), videoDetail);
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
            VideoPresenter.getInstance().likeVideo(getActivity(), videoDetail);
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
            if (disliked) {
                dislikeImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dislike_enabled));
                dislikeNum.setTextColor(getResources().getColor(R.color.tabColor, getContext().getTheme()));
                disliked = false;
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
            VideoPresenter.getInstance().dislikeVideo(getActivity(), videoDetail);
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
            if (liked) {
                likeImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.like_enabled));
                likeNum.setTextColor(getResources().getColor(R.color.tabColor, getContext().getTheme()));
                liked = false;
            }
        }
    };

    public RecyclerView getRelatedRecyclerView() {
        return relatedVideos;
    }

    public void enableDownload() {
        if (downloadImg == null || downloadNum == null || getContext() == null) return;
        if (downloaded) return;
        downloadEnabled = true;
        downloadImg.setImageResource(R.drawable.download_enabled);
        downloadNum.setTextColor(getContext().getColor(R.color.tabColor));
    }

    //刷新收藏状态
    public void refreshCollect() {
        if (collectImg == null || collectNum == null || getContext() == null) return;
        if (!DataHolder.getInstance().collectedVideo(videoDetail.getId())) {
            collectImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang_enabled));
        } else {
            collectImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang_clicked));
        }
        if (videoDetail.getCollected() > 0)
            collectNum.setText(String.valueOf(videoDetail.getCollected()));
        else collectNum.setText("收藏");
    }

    //刷新下载状态
    public void refreshDownload() {
        if (downloadNum == null || downloadImg == null) return;
        downloaded = WushanApp.getInstance().getDbHelper().downloaded(videoDetail.getId());
        if (downloaded) {
            downloadImg.setImageResource(R.drawable.download_clicked);
            downloadNum.setText("已下载");
            downloadNum.setTextColor(getContext().getColor(R.color.tabColor));
        } else {
            downloadImg.setImageResource(R.drawable.download_enabled);
            if (videoDetail.getDownloaded() > 0)
                downloadNum.setText(String.valueOf(videoDetail.getDownloaded()));
            else downloadNum.setText("下载");
        }
    }

    //刷新喜好
    public void refreshLikeAndDislike() {
        if (dislikeImg == null || dislikeNum == null || likeNum == null || likeImg == null) return;
        disliked = DataHolder.getInstance().dislikedVideo(videoDetail.getId());
        liked = DataHolder.getInstance().likedVideo(videoDetail.getId());
        if (disliked)
            dislikeImg.setImageResource(R.drawable.dislike_clicked);
        if (liked)
            likeImg.setImageResource(R.drawable.like_clicked);
        if (videoDetail.getLiked() > 0)
            likeNum.setText(String.valueOf(videoDetail.getLiked()));
        else likeNum.setText("喜欢");
        if (videoDetail.getDisliked() > 0)
            dislikeNum.setText(String.valueOf(videoDetail.getDisliked()));
        else dislikeNum.setText("不喜欢");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler = null;
    }
}
