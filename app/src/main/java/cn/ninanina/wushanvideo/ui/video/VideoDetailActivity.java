package cn.ninanina.wushanvideo.ui.video;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.network.VideoDetailPresenter;

public class VideoDetailActivity extends AppCompatActivity {
    @BindView(R.id.detail_player)
    StandardGSYVideoPlayer detailPlayer;
    @BindView(R.id.detail_tab)
    TabLayout tabLayout;
    @BindView(R.id.detail_viewpager)
    ViewPager2 viewPager2;

    private List<Fragment> fragments = new ArrayList<>(2);
    private List<String> tabTitles = new ArrayList<String>() {{
        add("详情");
        add("评论");
    }};

    OrientationUtils orientationUtils;
    GSYVideoOptionBuilder gsyVideoOption;


    private long videoId;
    private String title;
    private String titleZh;
    private int viewed;
    private String coverUrl;
    private ArrayList<String> tags;

    private String src;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_detail);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        videoId = intent.getLongExtra("id", 0);
        title = intent.getStringExtra("title");
        titleZh = intent.getStringExtra("titleZh");
        viewed = intent.getIntExtra("viewed", 0);
        coverUrl = intent.getStringExtra("coverUrl");
        tags = intent.getStringArrayListExtra("tags");

        initDetail();
        initPlayer();
    }

    private void initDetail() {
        tabLayout.setTabIndicatorFullWidth(false);
        tabLayout.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_BOTTOM);
        fragments.add(new DetailFragment(videoId, title, titleZh, viewed, tags));
        fragments.add(new CommentFragment());
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager2.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragments.get(position);
            }

            @Override
            public int getItemCount() {
                return fragments.size();
            }
        });
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> tab.setText(tabTitles.get(position))).attach();
    }

    private void initPlayer() {
        detailPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        detailPlayer.getBackButton().setVisibility(View.GONE);

        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, detailPlayer);
        orientationUtils.setEnable(false);

        gsyVideoOption = new GSYVideoOptionBuilder();
        gsyVideoOption.setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setRotateWithSystem(false)
                .setAutoFullWithSize(true)
                .setShowFullAnimation(false)
                .setCacheWithPlay(true)
                .setVideoTitle(title)
                .setSeekRatio(2.5f)
                .setVideoAllCallBack(new GSYSampleCallBack() {
                    @Override
                    public void onPrepared(String url, Object... objects) {
                        super.onPrepared(url, objects);
                    }

                    @Override
                    public void onQuitFullscreen(String url, Object... objects) {
                        super.onQuitFullscreen(url, objects);
                        if (orientationUtils != null) {
                            orientationUtils.backToProtVideo();
                        }
                        ViewGroup.LayoutParams params = detailPlayer.getLayoutParams();
                        params.height = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240, getResources().getDisplayMetrics()));
                        detailPlayer.setLayoutParams(params);
                    }

                    @Override
                    public void onEnterFullscreen(String url, Object... objects) {
                        super.onEnterFullscreen(url, objects);
                        ViewGroup.LayoutParams params = detailPlayer.getLayoutParams();
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        detailPlayer.setLayoutParams(params);
                    }
                });
        detailPlayer.getFullscreenButton().setOnClickListener(v -> {
            //直接横屏
            orientationUtils.resolveByClick();

            //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
            detailPlayer.startWindowFullscreen(VideoDetailActivity.this, true, true);
        });

        new VideoDetailPresenter(this).access(videoId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        detailPlayer.onVideoPause();
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
        detailPlayer.onVideoResume();
    }

    public void startPlaying(String src) {
        this.src = src;
        gsyVideoOption.setUrl(src).build(detailPlayer);
        detailPlayer.startPlayLogic();
    }

}