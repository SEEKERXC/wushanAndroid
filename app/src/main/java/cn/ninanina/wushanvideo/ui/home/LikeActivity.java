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
import cn.ninanina.wushanvideo.adapter.SingleVideoListAdapter;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoClickListener;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoOptionClickListener;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;

public class LikeActivity extends AppCompatActivity {
    @BindView(R.id.back)
    FrameLayout back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.swipe)
    public SwipeRefreshLayout swipe;
    @BindView(R.id.content)
    RecyclerView content;

    public boolean loading = false;
    public boolean loadingFinished = false;
    public int page = 0;
    public final int size = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.white, null), true);
        ButterKnife.bind(this);
        bindEvents();
        VideoPresenter.getInstance().getLikedVideos(this);
    }

    private void bindEvents() {
        back.setOnClickListener(v -> LikeActivity.this.finish());
        content.setLayoutManager(new LinearLayoutManager(this));
        content.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //列表中LastVisibleItem为倒数第二行时，加载更多
                if (manager.findLastVisibleItemPosition() + 1 >= manager.getItemCount() && !loading && !loadingFinished) {
                    page++;
                    VideoPresenter.getInstance().getLikedVideos(LikeActivity.this);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        swipe.setOnRefreshListener(() -> swipe.setRefreshing(false));
    }

    public void showData(List<VideoDetail> videoDetails) {
        SingleVideoListAdapter adapter = (SingleVideoListAdapter) content.getAdapter();
        if (adapter == null) {
            content.setAdapter(new SingleVideoListAdapter(new ArrayList<>(videoDetails),
                    new DefaultVideoClickListener(this),
                    new DefaultVideoOptionClickListener(this)));
        } else adapter.insert(new ArrayList<>(videoDetails));
    }
}