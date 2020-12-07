package cn.ninanina.wushanvideo.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.ui.video.DownloadActivity;
import cn.ninanina.wushanvideo.util.DialogManager;

public class HomeFragment extends Fragment {
    @BindView(R.id.home_tab)
    TabLayout tabLayout;
    @BindView(R.id.home_viewpager)
    ViewPager2 viewPager2;
    @BindView(R.id.home_search_button)
    CardView searchButton;
    @BindView(R.id.home_download)
    ImageButton downloadButton;
    @BindView(R.id.home_history)
    ImageButton historyButton;

    private List<Pair<String, String>> tabTitles = new ArrayList<Pair<String, String>>() {{
        add(Pair.of("recommend", "推荐"));
        add(Pair.of("hot", "精选"));
        add(Pair.of("asian", "亚洲"));
        add(Pair.of("west", "欧美"));
        add(Pair.of("lesbian", "女同"));
    }};

    private List<VideoListFragment> fragments = new ArrayList<>(6);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        for (Pair<String, String> title : tabTitles) {
            fragments.add(new VideoListFragment(title.getLeft()));
        }
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorPrimary, null));
        tabLayout.setTabIndicatorFullWidth(false);
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
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> tab.setText(tabTitles.get(position).getRight())).attach();

        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        });
        downloadButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DownloadActivity.class);
            startActivity(intent);
        });
        historyButton.setOnClickListener(v -> {
            if (!WushanApp.loggedIn()) {
                DialogManager.getInstance().newLoginDialog(getContext()).show();
            }
            Intent intent = new Intent(getContext(), HistoryActivity.class);
            startActivity(intent);
        });
    }
}
