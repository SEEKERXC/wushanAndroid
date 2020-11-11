package cn.ninanina.wushanvideo.ui.video;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.network.VideoDetailPresenter;

public class VideoDetailActivity extends AppCompatActivity {
    @BindView(R.id.detail_player)
    StandardGSYVideoPlayer player;
    OrientationUtils orientationUtils;

    private long videoId;
    private String title;
    private int viewed;
    private String coverUrl;
    private ArrayList<String> tags;

    private String src;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);
        ButterKnife.bind(this);
        player.getTitleTextView().setVisibility(View.VISIBLE);
        player.getBackButton().setVisibility(View.GONE);

        Intent intent = getIntent();
        videoId = intent.getLongExtra("id", 0);
        title = intent.getStringExtra("title");
        viewed = intent.getIntExtra("viewed", 0);
        coverUrl = intent.getStringExtra("coverUrl");
        tags = intent.getStringArrayListExtra("tags");

        //设置旋转
        orientationUtils = new OrientationUtils(this, player);
        //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
        player.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orientationUtils.resolveByClick();
            }
        });
        //是否可以滑动调整
        player.setIsTouchWiget(true);

        new VideoDetailPresenter(this).access(videoId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.onVideoPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.onVideoResume();
    }

    public void startPlaying(String src) {
        this.src = src;
        player.setUp(src, true, title);
        player.startPlayLogic();
    }

}