package cn.ninanina.wushanvideo.ui.video;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.DownloadingVideoAdapter;
import cn.ninanina.wushanvideo.model.bean.common.DownloadInfo;

public class DownloadingFragment extends Fragment {
    @BindView(R.id.list)
    RecyclerView recyclerView;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipeRefreshLayout;

    private ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        swipeRefreshLayout.setColorSchemeResources(R.color.tabColor);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(() -> swipeRefreshLayout.setRefreshing(false));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        executorService.scheduleAtFixedRate(() -> getActivity().runOnUiThread(() -> {
            if (getActivity() == null) return;
            Map<String, DownloadInfo> map = ((DownloadActivity) getActivity()).getDownloadService().getTasks();
            List<DownloadInfo> downloadInfoList = new ArrayList<>(map.values());
            if (recyclerView.getAdapter() == null) {
                recyclerView.setAdapter(new DownloadingVideoAdapter(downloadInfoList));
            } else
                ((DownloadingVideoAdapter) recyclerView.getAdapter()).update(downloadInfoList);
            swipeRefreshLayout.setRefreshing(false);
        }), 0, 200, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executorService.shutdownNow();
    }
}
