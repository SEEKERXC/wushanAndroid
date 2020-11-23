package cn.ninanina.wushanvideo.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.githang.statusbar.StatusBarCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.ui.community.CommunityFragment;
import cn.ninanina.wushanvideo.ui.home.HomeFragment;
import cn.ninanina.wushanvideo.ui.me.MeFragment;
import cn.ninanina.wushanvideo.ui.tag.TagFragment;
import cn.ninanina.wushanvideo.util.DialogManager;

public class MainActivity extends AppCompatActivity {
    private Fragment[] fragments;
    private HomeFragment homeFragment;
    private TagFragment tagFragment;
    private CommunityFragment communityFragment;
    private MeFragment meFragment;

    @BindView(R.id.bottom_navigation)
    public BottomNavigationView bottomNavigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.white, null), true);

        //初始化fragments
        initFragments();

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
                    case R.id.navigation_community:
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

    private void initFragments() {
        homeFragment = new HomeFragment();
        tagFragment = new TagFragment();
        communityFragment = new CommunityFragment();
        meFragment = new MeFragment();
        fragments = new Fragment[]{homeFragment, tagFragment, communityFragment, meFragment};

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_container, homeFragment)
                .show(homeFragment)
                .commit();

        bottomNavigationView.setItemTextColor(AppCompatResources.getColorStateList(this, R.color.black));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (WushanApp.loggedIn()) {
            if (DialogManager.getInstance() != null && DialogManager.getInstance().getLoginDialog() != null)
                DialogManager.getInstance().getLoginDialog().superDismiss();
        }
    }

}