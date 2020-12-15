package cn.ninanina.wushanvideo.ui.video;

import android.os.Bundle;
import android.os.Environment;
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
import java.util.Comparator;
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
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.common.VideoSortBy;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.util.CommonUtils;
import cn.ninanina.wushanvideo.util.FileUtil;
import cn.ninanina.wushanvideo.util.TimeUtil;

public class DownloadedFragment extends Fragment {
    @BindView(R.id.list)
    RecyclerView list;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipeRefreshLayout;

    public static Handler handler;
    public final static int update = 0;
    public final static int updateOne = 1;
    public final static int deleteOne = 2;

    public VideoSortBy sortBy = VideoSortBy.UPDATE_TIME;

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

    @Override
    public void onStop() {
        super.onStop();
        handler = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler = null;
    }

    public void loadData() {
        long totalLength = 0;
        File dir = FileUtil.getVideoDir();
        File[] videos = dir.listFiles();
        List<VideoDetail> videoDetails = new ArrayList<>();
        if (videos != null)
            for (File file : videos) {
                String name = file.getName();
                if (!name.endsWith(".mp4")) continue;
                VideoDetail videoDetail = WushanApp.getInstance().getDbHelper().getVideo(name);
                if (videoDetail == null) continue;
                if (videoDetail.getId() == null) continue;
                videoDetail.setSrc(dir.getAbsolutePath() + "/" + name);
                videoDetail.setUpdateTime(file.lastModified());
                videoDetail.setSize(file.length());
                videoDetails.add(videoDetail);
                totalLength += file.length();
            }
        switch (sortBy) {
            case UPDATE_TIME:
                Collections.sort(videoDetails, (o1, o2) -> o2.getUpdateTime().intValue() - o1.getUpdateTime().intValue());
                break;
            case PLAY:
                Collections.sort(videoDetails, (o1, o2) -> DataHolder.getInstance().viewedCount(o2.getId()) - DataHolder.getInstance().viewedCount(o1.getId()));
                break;
            case NAME:
                break;
            case SIZE:
                Collections.sort(videoDetails, (o1, o2) -> o2.getSize().intValue() - o1.getSize().intValue());
                break;
            case DURATION:
                Collections.sort(videoDetails, (o1, o2) -> CommonUtils.getDurationSeconds(o2.getDuration()) - CommonUtils.getDurationSeconds(o1.getDuration()));
                break;
        }
        list.setAdapter(new DownloadedVideoAdapter(new ArrayList<>(videoDetails),
                new OfflineVideoClickListener(getContext()),
                new DownloadOptionClickListener(getContext())));
        ((DownloadActivity) Objects.requireNonNull(getActivity())).statistics.setText("已用" + FileUtil.getSize(totalLength));
        swipeRefreshLayout.setRefreshing(false);
    }
}
