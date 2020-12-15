package cn.ninanina.wushanvideo.ui.video;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.githang.statusbar.StatusBarCompat;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.DownloadedVideoAdapter;
import cn.ninanina.wushanvideo.adapter.DownloadingVideoAdapter;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoOptionClickListener;
import cn.ninanina.wushanvideo.adapter.listener.OfflineVideoClickListener;
import cn.ninanina.wushanvideo.model.bean.common.DownloadInfo;
import cn.ninanina.wushanvideo.model.bean.common.VideoSortBy;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.service.DownloadService;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.util.DBHelper;
import cn.ninanina.wushanvideo.util.FileUtil;
import cn.ninanina.wushanvideo.util.LayoutUtil;

public class DownloadActivity extends AppCompatActivity {
    @BindView(R.id.back)
    FrameLayout back;
    @BindView(R.id.statistics)
    TextView statistics;
    @BindView(R.id.rank_content)
    LinearLayout rankButton;
    @BindView(R.id.rank)
    TextView rank;
    @BindView(R.id.tab)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager2 viewPager2;

    DownloadedFragment downloadedFragment;
    DownloadingFragment downloadingFragment;

    private DownloadService downloadService;
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

    private List<Fragment> fragments = new ArrayList<>(2);
    private List<String> tabTitles = new ArrayList<String>() {{
        add("已下载");
        add("正在下载");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.white, null), true);
        downloadedFragment = new DownloadedFragment();
        downloadingFragment = new DownloadingFragment();
        fragments.add(downloadedFragment);
        fragments.add(downloadingFragment);
        tabLayout.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_BOTTOM);
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
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> tab.setText(tabTitles.get(position))).attach();

        final Intent intent = new Intent(this, DownloadService.class);
        bindService(intent, downloadServiceConn, Service.BIND_AUTO_CREATE);

        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(() -> runOnUiThread(() -> {
            if (downloadService == null) return;
            int downloadedSize = WushanApp.getInstance().getDbHelper().getCount();
            tabLayout.getTabAt(0).setText("已下载（" + downloadedSize + "）");
            int downloadingSize = downloadService.getTasks().size();
            tabLayout.getTabAt(1).setText("正在下载（" + downloadingSize + "）");
        }), 0, 200, TimeUnit.MILLISECONDS);

        back.setOnClickListener(v -> DownloadActivity.this.finish());

        rankButton.setOnClickListener(v -> {
            LinearLayout rankView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.popup_download_rank, null, false);
            List<TextView> textViews = new ArrayList<TextView>() {{
                add(rankView.findViewById(R.id.updateTime));
                add(rankView.findViewById(R.id.viewed));
                add(rankView.findViewById(R.id.filename));
                add(rankView.findViewById(R.id.filesize));
                add(rankView.findViewById(R.id.duration));
            }};
            List<VideoSortBy> sortBIES = new ArrayList<VideoSortBy>() {{
                add(VideoSortBy.UPDATE_TIME);
                add(VideoSortBy.PLAY);
                add(VideoSortBy.NAME);
                add(VideoSortBy.SIZE);
                add(VideoSortBy.DURATION);
            }};
            PopupWindow popupWindow = new PopupWindow(rankView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setTouchModal(true);
            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.showAsDropDown(rankButton);
            for (int i = 0; i < textViews.size(); i++) {
                TextView textView = textViews.get(i);
                VideoSortBy sortBy = sortBIES.get(i);
                textView.setOnClickListener(v1 -> {
                    popupWindow.dismiss();
                    downloadedFragment.sortBy = sortBy;
                    rank.setText(sortBy.getMsg());
                    downloadedFragment.loadData();
                });
            }
        });
    }

    public DownloadService getDownloadService() {
        return downloadService;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(downloadServiceConn);
    }

}