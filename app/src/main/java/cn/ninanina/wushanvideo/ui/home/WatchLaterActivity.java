package cn.ninanina.wushanvideo.ui.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.githang.statusbar.StatusBarCompat;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.PlaylistVideoAdapter;
import cn.ninanina.wushanvideo.adapter.ToWatchAdapter;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoClickListener;
import cn.ninanina.wushanvideo.adapter.listener.ToWatchClickListener;
import cn.ninanina.wushanvideo.model.bean.common.Pair;
import cn.ninanina.wushanvideo.model.bean.video.ToWatch;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.model.bean.video.VideoUserViewed;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.util.TimeUtil;

public class WatchLaterActivity extends AppCompatActivity {
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    @BindView(R.id.content)
    RecyclerView content;
    @BindView(R.id.back)
    FrameLayout back;
    @BindView(R.id.search)
    ImageView search;
    @BindView(R.id.edit)
    TextView edit;

    public int page = 0;
    public final int size = 10;
    public boolean loading = false;
    public boolean loadingFinished = false;
    private String lastDay = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_later);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.white, null), true);
        ButterKnife.bind(this);
        bindEvents();
        VideoPresenter.getInstance().getWatchLater(this);
    }

    private void bindEvents() {
        back.setOnClickListener(v -> WatchLaterActivity.this.finish());
        content.setLayoutManager(new LinearLayoutManager(this));
        swipe.setRefreshing(true);
        swipe.setOnRefreshListener(() -> swipe.setRefreshing(false));
        content.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //列表中LastVisibleItem为倒数第二行时，加载更多
                if (manager.findLastVisibleItemPosition() + 1 >= manager.getItemCount() && !loading && !loadingFinished) {
                    page++;
                    VideoPresenter.getInstance().getWatchLater(WatchLaterActivity.this);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    public void showData(List<Pair<ToWatch, VideoDetail>> list) {
        List<Object> dataList = new ArrayList<>();
        for (Pair<ToWatch, VideoDetail> pair : list) {
            String day = TimeUtil.getDate(pair.getFirst().getAddTime());
            String today = TimeUtil.getDate(System.currentTimeMillis());
            String yesterday = TimeUtil.getDate(System.currentTimeMillis() - 1000 * 3600 * 24);
            if (day.equals(today)) day = "今天";
            if (day.equals(yesterday)) day = "昨天";
            if (!day.equals(lastDay)) {
                dataList.add(day);
            }
            dataList.add(pair);
            lastDay = day;
        }
        if (loadingFinished) dataList.add(Boolean.TRUE);
        else dataList.add(Boolean.FALSE);
        ToWatchAdapter adapter = (ToWatchAdapter) content.getAdapter();
        if (adapter == null) {
            content.setAdapter(new ToWatchAdapter(dataList, new DefaultVideoClickListener(this), new ToWatchClickListener(this)));
        } else {
            adapter.insert(dataList);
        }
        swipe.setRefreshing(false);
    }
}