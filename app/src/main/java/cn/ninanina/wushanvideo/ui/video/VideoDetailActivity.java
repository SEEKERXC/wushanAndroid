package cn.ninanina.wushanvideo.ui.video;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
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
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.listener.PlayerTouchListener;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.util.CommonUtils;
import cn.ninanina.wushanvideo.util.DBHelper;
import cn.ninanina.wushanvideo.util.FileUtil;

public class VideoDetailActivity extends AppCompatActivity {
    @BindView(R.id.detail_player)
    StyledPlayerView detailPlayer;
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

    SimpleExoPlayer player;

    private List<Fragment> fragments = new ArrayList<>(2);
    private List<String> tabTitles = new ArrayList<String>() {{
        add("详情");
        add("评论");
    }};

    FragmentStateAdapter adapter;

    private VideoDetail video;

    private int playerHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_video_detail);
        ButterKnife.bind(this);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.transparent, null), false);

        Intent intent = getIntent();
        video = (VideoDetail) intent.getSerializableExtra("video");

        initDetail();
        initPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
        shadow.setVisibility(View.VISIBLE);
        loading.setText("视频地址解析中...");
        cover.setAspectRatio(1.78f);
        cover.setImageURI(video.getCoverUrl());
        player = new SimpleExoPlayer.Builder(this).build();
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) cover.getLayoutParams();
        ConstraintLayout.LayoutParams layoutParams1 = (ConstraintLayout.LayoutParams) detailPlayer.getLayoutParams();
        layoutParams1.height = layoutParams.height;
        playerHeight = layoutParams.height;
        detailPlayer.setPlayer(player);
        detailPlayer.setControllerShowTimeoutMs(1500);
        detailPlayer.setShowNextButton(false);
        detailPlayer.setShowFastForwardButton(false);
        detailPlayer.setShowPreviousButton(false);
        detailPlayer.setShowRewindButton(false);
        DBHelper dbHelper = WushanApp.getInstance().getDbHelper();
        String name = dbHelper.getNameById(video.getId());
        if (!StringUtils.isEmpty(name)) name = FileUtil.getVideoDir() + "/" + name;
        video.setSrc(name);
        if (!CommonUtils.isSrcValid(video.getSrc()))
            VideoPresenter.getInstance().getSrcForDetail(this, video.getId());
        else {
            ((DetailFragment) fragments.get(0)).enableDownload();
            ((DetailFragment) fragments.get(0)).videoDetail.setSrc(video.getSrc());
            startPlaying(video.getSrc());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.play();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)
                detailPlayer.getLayoutParams();
        ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) cover.getLayoutParams();
        //检查屏幕的方向
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params1.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params1.height = ViewGroup.LayoutParams.MATCH_PARENT;
            detailPlayer.performClick();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = playerHeight;
            params1.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params1.height = playerHeight;
        }
    }

    public void startPlaying(String src) {
        DataHolder.getInstance().recordViewed(video.getId());
        loading.setVisibility(View.GONE);
        shadow.setVisibility(View.GONE);
        cover.setVisibility(View.GONE);
        video.setSrc(src);

        detailPlayer.postDelayed(() -> {
            ((DetailFragment) fragments.get(0)).enableDownload();
            ((DetailFragment) fragments.get(0)).videoDetail.setSrc(src);
        }, 1000);
        detailPlayer.performClick();
        // Build the media item.
        MediaItem mediaItem = MediaItem.fromUri(src);
        // Set the media item to be played.
        player.setMediaItem(mediaItem);
        // Prepare the player.
        player.prepare();
        // Start the playback.
        player.play();
        detailPlayer.setOnTouchListener(new PlayerTouchListener(parent, detailPlayer, video));
    }
}