package cn.ninanina.wushanvideo.ui.video;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.network.VideoListPresenter;
import me.gujun.android.taggroup.TagGroup;

public class DetailFragment extends Fragment {
    @BindView(R.id.video_detail_title)
    TextView titleTextView;
    @BindView(R.id.video_detail_info)
    TextView infoTextView;
    @BindView(R.id.video_detail_collect)
    ImageButton collectButton;
    @BindView(R.id.video_detail_collect_num)
    TextView collectNum;
    @BindView(R.id.video_detail_download)
    ImageButton downloadButton;
    @BindView(R.id.video_detail_download_num)
    TextView downloadNum;
    @BindView(R.id.video_detail_dislike)
    ImageButton dislikeButton;
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

    public DetailFragment(long videoId, String title, String titleZh, int viewed, ArrayList<String> tags) {
        super();
        this.videoId = videoId;
        this.title = title;
        this.titleZh = titleZh;
        this.viewed = viewed;
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

        viewed = viewed / 100;
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
        if (!StringUtils.isEmpty(titleZh)) title += ("（机翻：" + titleZh + "）");
        titleTextView.setText(title);
        videoTags.setTags(tags);

        VideoListPresenter.getInstance().getRelatedVideos(relatedVideos, videoId);

        dislikeButton.setBackground(titleTextView.getBackground());
        downloadButton.setBackground(titleTextView.getBackground());
        collectButton.setBackground(titleTextView.getBackground());

        if (collected)
            collectButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang_clicked));
        if (downloaded)
            downloadButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.xiazai_clicked));
        if (disliked)
            dislikeButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dislike_clicked));
        bindEvents();
    }

    private void bindEvents() {
        collectButton.setOnClickListener(collectListener);
        collectNum.setOnClickListener(collectListener);
        downloadButton.setOnClickListener(downloadListener);
        downloadNum.setOnClickListener(downloadListener);
        dislikeButton.setOnClickListener(dislikeListener);
        dislikeNum.setOnClickListener(dislikeListener);
    }

    View.OnClickListener collectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (collected) {
                collectButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang));
                collectNum.setTextColor(getResources().getColor(R.color.tabColor, getContext().getTheme()));
                collected = false;
            } else {
                collectButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang_clicked));
                collectNum.setTextColor(getResources().getColor(R.color.black, getContext().getTheme()));
                collected = true;
            }
        }
    };

    View.OnClickListener downloadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (downloaded) {
                Toast.makeText(getContext(), "已经下载了", Toast.LENGTH_SHORT).show();
                downloaded = false;
            } else {
                downloadButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.xiazai_clicked));
                downloadNum.setTextColor(getResources().getColor(R.color.black, getContext().getTheme()));
                downloaded = true;
            }
        }
    };

    View.OnClickListener dislikeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (disliked) {
                dislikeButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dislike));
                dislikeNum.setTextColor(getResources().getColor(R.color.tabColor, getContext().getTheme()));
                disliked = false;
            } else {
                dislikeButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dislike_clicked));
                dislikeNum.setTextColor(getResources().getColor(R.color.black, getContext().getTheme()));
                disliked = true;
            }
        }
    };
}
