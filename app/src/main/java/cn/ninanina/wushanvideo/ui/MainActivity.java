package cn.ninanina.wushanvideo.ui;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.githang.statusbar.StatusBarCompat;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;
import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.network.CommonPresenter;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.service.DownloadService;
import cn.ninanina.wushanvideo.ui.instant.InstantFragment;
import cn.ninanina.wushanvideo.ui.home.HomeFragment;
import cn.ninanina.wushanvideo.ui.me.MeFragment;
import cn.ninanina.wushanvideo.ui.tag.TagFragment;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;
import cn.ninanina.wushanvideo.util.PlayTimeManager;

public class MainActivity extends AppCompatActivity {
    private static MainActivity mainActivity;

    public Fragment[] fragments;
    private HomeFragment homeFragment;
    private TagFragment tagFragment;
    private InstantFragment instantFragment;
    private MeFragment meFragment;
    public int lastIndex = 0;

    @BindView(R.id.bottom_navigation)
    public BottomNavigationView bottomNavigationView;

    public DownloadService downloadService;
    private ServiceConnection downloadServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            DownloadService.DownloadBinder downloadBinder = (DownloadService.DownloadBinder) binder;
            downloadService = downloadBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public Stack<VideoDetailActivity> videoActivityStack = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.white, null), true);

        //初始化fragments
        initFragments();

        //初始化底部导航栏
        initBottomNavigation();

        //初始化下载服务
        final Intent intent = new Intent(this, DownloadService.class);
        bindService(intent, downloadServiceConn, Service.BIND_AUTO_CREATE);

        //预加载instant videos
        VideoPresenter.getInstance().loadInstantVideos();

        //检查版本
        CommonPresenter.getInstance().checkVersion(this);

        //获取用户协议
        CommonPresenter.getInstance().getProtocol();

        WushanApp.getInstance().addActivity(this);
    }

    private void initFragments() {
        homeFragment = new HomeFragment();
        tagFragment = new TagFragment();
        instantFragment = new InstantFragment();
        meFragment = new MeFragment();
        fragments = new Fragment[]{homeFragment, tagFragment, instantFragment, meFragment};

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_container, homeFragment)
                .show(homeFragment)
                .commit();

        bottomNavigationView.setItemTextColor(AppCompatResources.getColorStateList(this, R.color.black));
    }

    private void initBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        switchFragment(0);
                        StatusBarCompat.setStatusBarColor(MainActivity.this, getResources().getColor(android.R.color.white, null), true);
                        break;
                    case R.id.navigation_tag:
                        switchFragment(1);
                        break;
                    case R.id.navigation_instant:
                        switchFragment(2);
                        break;
                    case R.id.navigation_me:
                        switchFragment(3);
                        StatusBarCompat.setStatusBarColor(MainActivity.this, getResources().getColor(android.R.color.transparent, null), true);
                        meFragment.refreshPlaylist();
                        break;
                }
                return true;
            }

            public void switchFragment(int index) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.hide(fragments[lastIndex]);
                lastIndex = index;
                if (!fragments[index].isAdded()) {
                    transaction.add(R.id.main_container, fragments[index]);
                }
                transaction.show(fragments[index]).commitAllowingStateLoss();
                getSupportFragmentManager().executePendingTransactions();

                StyledPlayerView playerView = ((InstantFragment) fragments[2]).getPlayerView();
                if (index != 2 && playerView != null) {
                    Objects.requireNonNull(playerView.getPlayer()).pause();
                } else if (index == 2 && playerView != null) {
                    Objects.requireNonNull(playerView.getPlayer()).play();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PlayTimeManager.stopTiming();
        WushanApp.getInstance().removeActivity(this);
    }

    public static MainActivity getInstance() {
        return mainActivity;
    }

}