package cn.ninanina.wushanvideo.ui.home;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.flyco.dialog.widget.MaterialDialog;
import com.githang.statusbar.StatusBarCompat;
import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.HistoryAdapter;
import cn.ninanina.wushanvideo.adapter.SingleVideoListAdapter;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoClickListener;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoOptionClickListener;
import cn.ninanina.wushanvideo.adapter.listener.HistoryClickListener;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.common.Pair;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.model.bean.video.VideoUserViewed;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.util.DialogManager;
import cn.ninanina.wushanvideo.util.TimeUtil;

public class HistoryActivity extends AppCompatActivity {

    @BindView(R.id.swipe)
    public SwipeRefreshLayout swipe;
    @BindView(R.id.content)
    RecyclerView content;
    @BindView(R.id.back)
    FrameLayout back;
    @BindView(R.id.delete)
    FrameLayout delete;

    public int page = 0;
    public final int size = 10;
    public long startOfDay = 0;
    private boolean byTimeDesc = true;

    public boolean loading = false;
    public boolean loadingFinished = false;

    private String lastDay = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.white, null), true);
        swipe.setColorSchemeResources(R.color.tabColor);
        swipe.setRefreshing(true);
        VideoPresenter.getInstance().getHistoryVideos(this);
        WushanApp.getInstance().addActivity(this);

        initEvents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WushanApp.getInstance().removeActivity(this);
    }

    private void initEvents() {
        back.setOnClickListener(v -> HistoryActivity.this.finish());
        delete.setOnClickListener(v -> {
            if (!CollectionUtils.isEmpty(DataHolder.getInstance().getAllViewed()))
                DialogManager.getInstance().newDeleteAllHistoryDialog(this).show();
        });
        content.setLayoutManager(new LinearLayoutManager(this));
        content.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //列表中LastVisibleItem为倒数第二行时，加载更多
                if (manager.findLastVisibleItemPosition() + 1 >= manager.getItemCount() && !loading && !loadingFinished) {
                    page++;
                    VideoPresenter.getInstance().getHistoryVideos(HistoryActivity.this);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        swipe.setOnRefreshListener(() -> swipe.setRefreshing(false));
    }

    public void showData(List<Pair<VideoUserViewed, VideoDetail>> data) {
        List<Object> dataList = new ArrayList<>();
        for (Pair<VideoUserViewed, VideoDetail> pair : data) {
            String day = TimeUtil.getDate(pair.getFirst().getTime());
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
        HistoryAdapter adapter = (HistoryAdapter) content.getAdapter();
        if (adapter == null) {
            content.setAdapter(new HistoryAdapter(dataList, new DefaultVideoClickListener(this), new HistoryClickListener(this)));
        } else {
            adapter.insert(dataList);
        }

    }

    public void delete(List<VideoUserViewed> vieweds) {
        HistoryAdapter adapter = (HistoryAdapter) content.getAdapter();
        if (adapter != null)
            adapter.delete(vieweds);
    }

    public void deleteAll() {
        HistoryAdapter adapter = (HistoryAdapter) content.getAdapter();
        if (adapter != null)
            adapter.deleteAll();
    }

}