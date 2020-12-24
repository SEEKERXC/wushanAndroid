package cn.ninanina.wushanvideo.ui.me;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.githang.statusbar.StatusBarCompat;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
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
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.InstantVideoAdapter;
import cn.ninanina.wushanvideo.network.AdManager;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.login_banner)
    Banner<Integer, BannerAdAdapter> banner;
    @BindView(R.id.login_tab)
    TabLayout tabLayout;
    @BindView(R.id.login_viewpager)
    ViewPager2 viewPager2;

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
        if (AdManager.getInstance().size() >= 3) {
            List<UnifiedNativeAd> ads = AdManager.getInstance().nextAds(3);
            banner.addBannerLifecycleObserver(this)
                    .setAdapter(new BannerAdAdapter(ads))
                    .setIndicator(new CircleIndicator(this));
        }
        WushanApp.getInstance().addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WushanApp.getInstance().removeActivity(this);
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

    static class BannerAdAdapter extends BannerAdapter<UnifiedNativeAd, BannerAdAdapter.AdViewHolder> {
        public BannerAdAdapter(List<UnifiedNativeAd> data) {
            super(data);
        }

        @Override
        public AdViewHolder onCreateHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner_ad, parent, false);
            return new BannerAdAdapter.AdViewHolder(view);
        }

        @Override
        public void onBindView(BannerAdAdapter.AdViewHolder holder, UnifiedNativeAd data, int position, int size) {
            holder.adView.setNativeAd(data);
            holder.headline.setText(data.getHeadline());
        }

        static class AdViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.ad_view)
            UnifiedNativeAdView adView;
            @BindView(R.id.ad_media)
            MediaView mediaView;
            @BindView(R.id.ad_headline)
            TextView headline;

            public AdViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                adView.setMediaView(mediaView);
                adView.setHeadlineView(headline);
            }
        }
    }
}