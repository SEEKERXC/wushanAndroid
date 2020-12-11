package cn.ninanina.wushanvideo.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.task.DownloadTask;
import com.githang.statusbar.StatusBarCompat;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.ui.instant.InstantFragment;
import cn.ninanina.wushanvideo.ui.home.HomeFragment;
import cn.ninanina.wushanvideo.ui.me.MeFragment;
import cn.ninanina.wushanvideo.ui.tag.TagFragment;
import cn.ninanina.wushanvideo.ui.video.DetailFragment;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;
import cn.ninanina.wushanvideo.util.DBHelper;

public class MainActivity extends AppCompatActivity {
    private static MainActivity mainActivity;

    private Fragment[] fragments;
    private HomeFragment homeFragment;
    private TagFragment tagFragment;
    private InstantFragment instantFragment;
    private MeFragment meFragment;
    public int lastIndex = 0;

    @BindView(R.id.bottom_navigation)
    public BottomNavigationView bottomNavigationView;

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
                        meFragment.refresh();
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
                StyledPlayerView playerView = ((InstantFragment) fragments[2]).getPlayerView();
                if (index != 2 && playerView != null) {
                    Objects.requireNonNull(playerView.getPlayer()).pause();
                } else if (index == 2 && playerView != null) {
                    Objects.requireNonNull(playerView.getPlayer()).play();
                }
            }
        });
    }

    //在登录之后初始化数据
    public void initData() {
        VideoPresenter.getInstance().loadPlaylists();
        VideoPresenter.getInstance().loadLikedAndDisliked(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public static MainActivity getInstance() {
        return mainActivity;
    }

}