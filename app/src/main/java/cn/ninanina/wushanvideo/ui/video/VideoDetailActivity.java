package cn.ninanina.wushanvideo.ui.video;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.facebook.drawee.view.SimpleDraweeView;
import com.githang.statusbar.StatusBarCompat;
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
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.ui.MainActivity;

public class VideoDetailActivity extends AppCompatActivity {
    @BindView(R.id.detail_player)
    StandardGSYVideoPlayer detailPlayer;
    @BindView(R.id.detail_cover)
    SimpleDraweeView cover;
    @BindView(R.id.detail_tab)
    TabLayout tabLayout;
    @BindView(R.id.video_detail_loading_shadow)
    View shadow;
    @BindView(R.id.video_detail_loading)
    TextView loading;
    @BindView(R.id.detail_viewpager)
    ViewPager2 viewPager2;
    @BindView(R.id.video_detail_parent)
    ConstraintLayout parent;

    private List<Fragment> fragments = new ArrayList<>(2);
    private List<String> tabTitles = new ArrayList<String>() {{
        add("详情");
        add("评论");
    }};

    OrientationUtils orientationUtils;
    GSYVideoOptionBuilder gsyVideoOption;

    FragmentStateAdapter adapter;

    private VideoDetail video;

    private String src;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_video_detail);
        ButterKnife.bind(this);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.transparent, null), false);

        Intent intent = getIntent();
        video = (VideoDetail) intent.getSerializableExtra("video");

        initDetail();
        initPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MainActivity.getInstance().setDetailFragment(null);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //如果是点击事件，获取点击的view，并判断是否要收起键盘
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //获取目前得到焦点的view
            View v = getCurrentFocus();
            //判断是否要收起并进行处理
            if (shouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        //这个是activity的事件分发，一定要有，不然就不会有任何的点击事件了
        return super.dispatchTouchEvent(ev);
    }

    private boolean shouldHideKeyboard(View v, MotionEvent event) {
        //如果目前得到焦点的这个view是editText的话进行判断点击的位置
        if (v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            // 点击EditText的事件，忽略它。
            return !(event.getX() > left) || !(event.getX() < right)
                    || !(event.getY() > top) || !(event.getY() < bottom);
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上
        return false;
    }

    //隐藏软键盘并让editText失去焦点
    private void hideKeyboard(IBinder token) {
        EditText input = ((CommentFragment) fragments.get(1)).input;
        if (input != null) input.clearFocus();
        if (token != null) {
            //这里先获取InputMethodManager再调用他的方法来关闭软键盘
            //InputMethodManager就是一个管理窗口输入的manager
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (im != null) {
                im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    private void initDetail() {
        tabLayout.setTabIndicatorFullWidth(false);
        tabLayout.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_BOTTOM);
        fragments.add(new DetailFragment(video));
        fragments.add(new CommentFragment(video));
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        adapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragments.get(position);
            }

            @Override
            public int getItemCount() {
                return fragments.size();
            }
        };
        viewPager2.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> tab.setText(tabTitles.get(position))).attach();
    }

    private void initPlayer() {
        detailPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        detailPlayer.getBackButton().setVisibility(View.GONE);
        shadow.setVisibility(View.VISIBLE);
        loading.setText("视频地址解析中，请等待几秒...");
        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, detailPlayer);
        orientationUtils.setEnable(true);
        cover.setImageURI(video.getCoverUrl());
        gsyVideoOption = new GSYVideoOptionBuilder();
        gsyVideoOption.setIsTouchWiget(true)
                .setRotateViewAuto(true)
                .setRotateWithSystem(true)
                .setAutoFullWithSize(true)
                .setShowFullAnimation(false)
                .setCacheWithPlay(true)
                .setVideoTitle(video.getTitle())
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
                        viewPager2.setAdapter(adapter);
                        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> tab.setText(tabTitles.get(position))).attach();
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

        VideoPresenter.getInstance().getSrc(this, video.getId());
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
        parent.removeView(loading);
        parent.removeView(shadow);
        parent.removeView(cover);
        this.src = src;
        ((DetailFragment) fragments.get(0)).enableDownload();
        gsyVideoOption.setUrl(src).build(detailPlayer);
        detailPlayer.startPlayLogic();
    }

    public String getSrc() {
        return src;
    }
}