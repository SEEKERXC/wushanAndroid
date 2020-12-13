package cn.ninanina.wushanvideo.ui.video;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.DownloadedVideoAdapter;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoOptionClickListener;
import cn.ninanina.wushanvideo.adapter.listener.DownloadOptionClickListener;
import cn.ninanina.wushanvideo.adapter.listener.OfflineVideoClickListener;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.util.FileUtil;

public class DownloadedFragment extends Fragment {
    @BindView(R.id.list)
    RecyclerView list;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipeRefreshLayout;

    public static Handler handler;
    public final static int update = 0;
    public final static int updateOne = 1;
    public final static int deleteOne = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout.setColorSchemeResources(R.color.tabColor);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(this::loadData);
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == update) {
                    loadData();
                } else if (msg.what == updateOne) {
                    DownloadedVideoAdapter adapter = (DownloadedVideoAdapter) list.getAdapter();
                    Objects.requireNonNull(adapter).updateOne(msg.arg1);
                } else if (msg.what == deleteOne) {
                    DownloadedVideoAdapter adapter = (DownloadedVideoAdapter) list.getAdapter();
                    Objects.requireNonNull(adapter).deleteOne(msg.arg1);
                }
                super.handleMessage(msg);
            }
        };
        loadData();
    }

    private void loadData() {
        File dir = FileUtil.getVideoDir();
        File[] videos = dir.listFiles();
        List<VideoDetail> videoDetails = new ArrayList<>();
        if (videos != null)
            for (File file : videos) {
                String name = file.getName();
                if (!name.endsWith(".mp4")) continue;
                VideoDetail videoDetail = WushanApp.getInstance().getDbHelper().getVideo(name);
                if (videoDetail.getId() == null) continue;
                videoDetail.setSrc(dir.getAbsolutePath() + "/" + name);
                videoDetail.setUpdateTime(file.lastModified());
                videoDetail.setSize(file.length());
                videoDetails.add(videoDetail);
            }
        Collections.sort(videoDetails, (o1, o2) -> o2.getUpdateTime().intValue() - o1.getUpdateTime().intValue());
        list.setAdapter(new DownloadedVideoAdapter(new ArrayList<>(videoDetails),
                new OfflineVideoClickListener(getContext()),
                new DownloadOptionClickListener(getContext())));
        swipeRefreshLayout.setRefreshing(false);
    }
}
