package cn.ninanina.wushanvideo.ui.instant;

import android.app.DownloadManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.InstantVideoAdapter;
import cn.ninanina.wushanvideo.adapter.listener.DefaultDownloadClickListener;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoClickListener;
import cn.ninanina.wushanvideo.adapter.listener.PlayerTouchListener;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.service.DownloadService;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.ui.home.VideoListFragment;
import cn.ninanina.wushanvideo.util.CommonUtils;
import cn.ninanina.wushanvideo.util.DialogManager;
import cn.ninanina.wushanvideo.util.PermissionUtils;

/**
 * 这一页展示当下所有有现成链接的视频，以列表的形式呈现出来，模仿西瓜视频首页的形式，用户滑动即可立即播放
 */
public class InstantFragment extends Fragment {
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    public int limit = 10;
    public boolean loading = false;

    private int firstCompletelyVisible = -1;
    private StyledPlayerView playerView; //正在播放的player
    private List<SimpleExoPlayer> players = new ArrayList<>(); //保存播放了的player，用于释放资源

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_instant, container, false);
    }

    @Override //TODO:只播放完全显示的，上面和下面较远的全部释放
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        swipe.setRefreshing(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (manager.findLastVisibleItemPosition() + 1 >= manager.getItemCount() && !loading) {
                    VideoPresenter.getInstance().getInstantVideos(InstantFragment.this);
                }
                int firstCompletePosition = manager.findFirstCompletelyVisibleItemPosition();
                if (firstCompletePosition == firstCompletelyVisible) return;
                firstCompletelyVisible = firstCompletePosition;
                //释放上面的和下面的player
                if (firstCompletePosition >= 3) {
                    players.get(firstCompletePosition - 3).release();
                }
                if (players.size() - firstCompletePosition > 3) {
                    for (int i = players.size() - 1; i > players.size() - firstCompletePosition; i--) {
                        players.get(i).release();
                    }
                }
                //暂停播放之前的视频
                if (playerView != null) {
                    Objects.requireNonNull(playerView.getPlayer()).pause();
                }
                View firstView = manager.findViewByPosition(firstCompletePosition);
                playerView = firstView.findViewById(R.id.player);
                if (playerView != null) {
                    SimpleExoPlayer player = (SimpleExoPlayer) playerView.getPlayer();
                    assert player != null;
                    if (player.isPlaying()) return;
                    //判断src是否有效
                    InstantVideoAdapter adapter = (InstantVideoAdapter) recyclerView.getAdapter();
                    VideoDetail videoDetail = (VideoDetail) adapter.getDataList().get(firstCompletePosition);
                    if (CommonUtils.isSrcValid(videoDetail.getSrc())) {
                        player.prepare();
                        player.play();
                        if (!players.contains(player)) players.add(player);
                    } else {
                        // TODO: 2020/12/8 0008 获取视频链接并播放
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        //连接到下载服务
        final Intent intent = new Intent(getContext(), DownloadService.class);
        Objects.requireNonNull(getContext()).bindService(intent, downloadServiceConn, Service.BIND_AUTO_CREATE);
        VideoPresenter.getInstance().getInstantVideos(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (playerView != null) Objects.requireNonNull(playerView.getPlayer()).pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (playerView != null && MainActivity.getInstance().lastIndex == 2)
            Objects.requireNonNull(playerView.getPlayer()).play();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(getContext()).unbindService(downloadServiceConn);
    }

    public void showData(List<VideoDetail> videoDetails) {
        if (recyclerView.getAdapter() == null) {
            InstantVideoAdapter adapter = new InstantVideoAdapter(getContext(), new ArrayList<>(videoDetails))
                    .setVideoClickListener(new DefaultVideoClickListener(getContext()))
                    .setDownloadListener(new DefaultDownloadClickListener(downloadService));
            recyclerView.setAdapter(adapter);
        } else {
            InstantVideoAdapter adapter = (InstantVideoAdapter) recyclerView.getAdapter();
            adapter.insert(new ArrayList<>(videoDetails));
        }
        this.swipe.setRefreshing(false);
    }

    public StyledPlayerView getPlayerView() {
        return playerView;
    }

}
