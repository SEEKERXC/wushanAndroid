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
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

public class MainActivity extends AppCompatActivity {
    private static MainActivity mainActivity;

    private Fragment[] fragments;
    private HomeFragment homeFragment;
    private TagFragment tagFragment;
    private InstantFragment instantFragment;
    private MeFragment meFragment;

    @BindView(R.id.bottom_navigation)
    public BottomNavigationView bottomNavigationView;
    //传递下载数据
    private DetailFragment detailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.white, null), true);
        Aria.download(this).register(); //初始化下载，所有下载相关都注册到这个activity

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
            int lastIndex = 0;

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
            }
        });
    }

    //初始化数据
    public void initData() {
        VideoPresenter.getInstance().loadPlaylists();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public static MainActivity getInstance() {
        return mainActivity;
    }

    public MeFragment getMeFragment() {
        return meFragment;
    }

    public void setDetailFragment(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    public DetailFragment getDetailFragment() {
        return detailFragment;
    }

    @Download.onTaskComplete
    void taskComplete(DownloadTask task) {
        if (detailFragment == null) return;
        //在这里处理任务完成的状态
        if (task.getKey().equals(((VideoDetailActivity) detailFragment.getActivity()).getSrc())) {
            Toast.makeText(this, "下载完成", Toast.LENGTH_SHORT).show();
            detailFragment.finishDownload();
        }
    }

    @Download.onTaskRunning
    void running(DownloadTask task) {
        if (detailFragment != null
                && detailFragment.getActivity() != null
                && task.getKey().equals(((VideoDetailActivity) detailFragment.getActivity()).getSrc())) {
            int p = task.getPercent();  //任务进度百分比
            String speed = task.getConvertSpeed();  //转换单位后的下载速度，单位转换需要在配置文件中打开
            detailFragment.getDownloadNum().setText(p + "%");
        }
    }


}