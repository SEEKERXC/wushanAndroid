package cn.ninanina.wushanvideo.ui.video;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.common.util.CollectionUtils;
import com.orhanobut.dialogplus.DialogPlus;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.SingleVideoListAdapter;
import cn.ninanina.wushanvideo.model.bean.video.Tag;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.ui.me.LoginActivity;
import cn.ninanina.wushanvideo.util.DialogManager;
import me.gujun.android.taggroup.TagGroup;

public class DetailFragment extends Fragment {
    @BindView(R.id.video_detail_title)
    TextView titleTextView;
    @BindView(R.id.video_detail_info)
    TextView infoTextView;
    @BindView(R.id.video_detail_collect_button)
    ConstraintLayout collectButton;
    @BindView(R.id.video_detail_collect_img)
    ImageButton collectImg;
    @BindView(R.id.video_detail_collect_num)
    TextView collectNum;
    @BindView(R.id.video_detail_download_button)
    ConstraintLayout downloadButton;
    @BindView(R.id.video_detail_download_img)
    ImageButton downloadImg;
    @BindView(R.id.video_detail_download_num)
    TextView downloadNum;
    @BindView(R.id.video_detail_dislike_button)
    ConstraintLayout dislikeButton;
    @BindView(R.id.video_detail_dislike_img)
    ImageButton dislikeImg;
    @BindView(R.id.video_detail_dislike_num)
    TextView dislikeNum;
    @BindView(R.id.video_detail_tags)
    TagGroup videoTags;
    @BindView(R.id.detail_related_videos)
    RecyclerView relatedVideos;

    private long videoId;
    private String title;
    private String titleZh;
    private int viewed;
    private ArrayList<String> tags;
    private boolean collected;
    private boolean downloaded;
    private boolean disliked;

    YoYo.YoYoString downloadAnimation;

    AlertDialog.Builder loginDialog;

    List<Object> dataList = new ArrayList<>();

    SingleVideoListAdapter.ItemClickListener itemClickListener = videoDetail -> {
        Intent intent = new Intent(getActivity(), VideoDetailActivity.class);
        intent.putExtra("id", videoDetail.getId());
        intent.putExtra("title", videoDetail.getTitle());
        intent.putExtra("titleZh", videoDetail.getTitleZh());
        intent.putExtra("viewed", videoDetail.getViewed());
        intent.putExtra("coverUrl", videoDetail.getCoverUrl());
        ArrayList<String> tags = new ArrayList<>();
        for (Tag tag : videoDetail.getTags()) {
            if (!StringUtils.isEmpty(tag.getTagZh()) && !tags.contains(tag.getTagZh()))
                tags.add(tag.getTagZh());
            else tags.add(tag.getTag());
        }
        if (videoDetail.getTags().isEmpty()) tags.add("无标签");
        intent.putStringArrayListExtra("tags", tags);
        startActivity(intent);
    };

    SingleVideoListAdapter.OptionsClickListener optionsClickListener = videoDetail -> {
        DialogPlus dialog = DialogManager.getInstance().newVideoOptionDialog(getContext(), videoDetail);
        dialog.show();
    };

    public DetailFragment(long videoId, String title, String titleZh, int viewed, ArrayList<String> tags) {
        super();
        this.videoId = videoId;
        this.title = title;
        this.titleZh = titleZh;
        this.viewed = viewed / 100;
        this.tags = tags;
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
        if (viewed >= 10000) {
            int wan = viewed / 10000;
            int qian = viewed % 10000 / 1000;
            strViewed.append(wan);
            if (qian != 0) strViewed.append('.').append(qian);
            strViewed.append("万");
        } else strViewed.append(viewed);
        strViewed.append("播放");
        infoTextView.setText(strViewed.toString());
        StringBuilder titleBuilder = new StringBuilder(title);
        if (!StringUtils.isEmpty(titleZh)) titleBuilder.append("（机翻：").append(titleZh).append("）");
        titleTextView.setText(titleBuilder.toString());
        videoTags.setTags(tags);

        relatedVideos.setLayoutManager(new LinearLayoutManager(getContext()));
        if (CollectionUtils.isEmpty(dataList))
            VideoPresenter.getInstance().getRelatedVideos(this, videoId);
        else
            relatedVideos.setAdapter(new SingleVideoListAdapter(dataList, itemClickListener, optionsClickListener));
        initLoginDialog();

        if (collected)
            collectImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang_clicked));
        if (downloaded)
            downloadImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.xiazai_clicked));
        if (disliked)
            downloadImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dislike_clicked));
        bindEvents();
    }

    private void bindEvents() {
        collectButton.setOnClickListener(collectListener);
        collectImg.setOnClickListener(collectListener);
        collectNum.setOnClickListener(collectListener);
        downloadButton.setOnClickListener(downloadListener);
        downloadImg.setOnClickListener(downloadListener);
        downloadNum.setOnClickListener(downloadListener);
        dislikeButton.setOnClickListener(dislikeListener);
        dislikeImg.setOnClickListener(dislikeListener);
        dislikeNum.setOnClickListener(dislikeListener);
    }

    View.OnClickListener collectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!WushanApp.loggedIn()) {
                loginDialog.show();
                return;
            }
            YoYo.with(Techniques.RubberBand)
                    .duration(500)
                    .playOn(collectImg);
            if (collected) {
                collectImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang));
                collectNum.setTextColor(getResources().getColor(R.color.tabColor, getContext().getTheme()));
                collected = false;
            } else {
                collectImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang_clicked));
                collectNum.setTextColor(getResources().getColor(R.color.black, getContext().getTheme()));
                collected = true;
            }
        }
    };

    View.OnClickListener downloadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!WushanApp.loggedIn()) {
                loginDialog.show();
                return;
            }
            downloadAnimation = YoYo.with(Techniques.SlideOutDown)
                    .duration(1000)
                    .repeat(9999)
                    .playOn(downloadImg);
            if (downloaded) {
                Toast.makeText(getContext(), "已经下载了", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "开始下载视频", Toast.LENGTH_SHORT).show();
                downloadImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.xiazai_clicked));
                downloadNum.setTextColor(getResources().getColor(R.color.black, getContext().getTheme()));
                downloaded = true;
            }
        }
    };

    View.OnClickListener dislikeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!WushanApp.loggedIn()) {
                loginDialog.show();
                return;
            }
            YoYo.with(Techniques.Tada)
                    .duration(500)
                    .playOn(dislikeImg);
            if (disliked) {
                dislikeImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dislike));
                dislikeNum.setTextColor(getResources().getColor(R.color.tabColor, getContext().getTheme()));
                disliked = false;
            } else {
                dislikeImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dislike_clicked));
                dislikeNum.setTextColor(getResources().getColor(R.color.black, getContext().getTheme()));
                disliked = true;
            }
        }
    };

    private void initLoginDialog() {
        loginDialog = new AlertDialog.Builder(getContext());
        loginDialog.setIcon(R.drawable.wode_ea5a5a);
        loginDialog.setMessage("是否立即注册/登录？");
        loginDialog.setPositiveButton("确定", (dialog, which) -> {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
        });
        loginDialog.setNegativeButton("取消", ((dialog, which) -> {
        }));
    }

    public RecyclerView getRelatedRecyclerView() {
        return relatedVideos;
    }

    public SingleVideoListAdapter.ItemClickListener getItemClickListener() {
        return itemClickListener;
    }

    public SingleVideoListAdapter.OptionsClickListener getOptionsClickListener() {
        return optionsClickListener;
    }

    public List<Object> getDataList() {
        return dataList;
    }
}
