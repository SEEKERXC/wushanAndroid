package cn.ninanina.wushanvideo.ui.video;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import co.lujun.androidtagview.TagContainerLayout;

public class DetailFragment extends Fragment {
    @BindView(R.id.video_detail_title)
    TextView titleTextView;
    @BindView(R.id.video_detail_info)
    TextView infoTextView;
    @BindView(R.id.video_detail_collect)
    ImageButton collectButton;
    @BindView(R.id.video_detail_download)
    ImageButton downloadButton;
    @BindView(R.id.video_detail_dislike)
    ImageButton dislikeButton;
    @BindView(R.id.video_detail_tags)
    TagContainerLayout videoTags;
    @BindView(R.id.detail_related_videos)
    ListView relatedVideos;

    private long videoId;
    private String title;
    private int viewed;
    private ArrayList<String> tags;
    private boolean collected;
    private boolean downloaded;
    private boolean disliked;

    public DetailFragment(long videoId, String title, int viewed, ArrayList<String> tags) {
        super();
        this.videoId = videoId;
        this.title = title;
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

        infoTextView.setText(viewed + "播放");
        titleTextView.setText(title);
        videoTags.setTags(tags);

        if (collected)
            collectButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang_clicked));
        if (downloaded)
            downloadButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.xiazai_clicked));
        if (disliked)
            dislikeButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dislike_clicked));
        bindEvents();
    }

    private void bindEvents() {
        collectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (collected) {
                    collectButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang));
                } else {
                    collectButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang_clicked));
                }

            }
        });
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downloaded)
                    Toast.makeText(getContext(), "已经下载了", Toast.LENGTH_SHORT).show();
                else {
                    downloadButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.xiazai_clicked));
                }
            }
        });
        dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (disliked) {
                    dislikeButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dislike));
                } else {
                    dislikeButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dislike_clicked));
                }

            }
        });
    }
}
