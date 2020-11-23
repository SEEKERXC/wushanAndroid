package cn.ninanina.wushanvideo.ui.me;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.githang.statusbar.StatusBarCompat;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerAdapter;
import com.youth.banner.indicator.CircleIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.login_banner)
    Banner<Integer, ImageAdapter> banner;
    @BindView(R.id.login_tab)
    TabLayout tabLayout;
    @BindView(R.id.login_viewpager)
    ViewPager2 viewPager2;

    List<Integer> covers = new ArrayList<Integer>() {{
        add(R.drawable.cover1);
        add(R.drawable.cover2);
        add(R.drawable.cover3);
    }};

    private List<String> tabTitles = new ArrayList<String>() {{
        add("注册");
        add("登录");
    }};

    private List<Fragment> fragments = new ArrayList<>(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.transparent, null), true);
        ButterKnife.bind(this);
        initFragments();
        banner.addBannerLifecycleObserver(this)
                .setAdapter(new ImageAdapter(covers))
                .setIndicator(new CircleIndicator(this));
    }

    private void initFragments() {
        fragments.add(new RegisterFragment());
        fragments.add(new LoginFragment());
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorPrimary, null));
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

    static class ImageAdapter extends BannerAdapter<Integer, ImageAdapter.BannerViewHolder> {

        public ImageAdapter(List<Integer> mDatas) {
            //设置数据，也可以调用banner提供的方法,或者自己在adapter中实现
            super(mDatas);
        }

        //创建ViewHolder，可以用viewType这个字段来区分不同的ViewHolder
        @Override
        public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            //注意，必须设置为match_parent，这个是viewpager2强制要求的
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new BannerViewHolder(imageView);
        }

        @Override
        public void onBindView(BannerViewHolder holder, Integer data, int position, int size) {
            holder.imageView.setImageResource(data);
        }

        class BannerViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public BannerViewHolder(@NonNull ImageView view) {
                super(view);
                this.imageView = view;
            }
        }
    }

}