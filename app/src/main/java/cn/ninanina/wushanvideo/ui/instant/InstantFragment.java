package cn.ninanina.wushanvideo.ui.instant;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.InstantVideoAdapter;
import cn.ninanina.wushanvideo.adapter.listener.DefaultDownloadClickListener;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoClickListener;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.AdManager;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.service.DownloadService;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.util.CommonUtils;

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
    private List<SimpleDraweeView> covers = new ArrayList<>();

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

    public Handler handler;
    public static final int updateData = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_instant, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        swipe.setRefreshing(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.getRecycledViewPool().setMaxRecycledViews(InstantVideoAdapter.TYPE_VIDEO, 50);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            Set<Object> ads = new HashSet<>();

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (manager.findLastVisibleItemPosition() + 1 >= manager.getItemCount() && !loading) {
                    VideoPresenter.getInstance().getInstantVideos(InstantFragment.this, false);
                }
                int firstCompletePosition = manager.findFirstCompletelyVisibleItemPosition();
                if (firstCompletePosition < 0) return;
                if (!(((InstantVideoAdapter) recyclerView.getAdapter()).dataList.get(firstCompletePosition) instanceof VideoDetail)) {
                    ads.add(((InstantVideoAdapter) recyclerView.getAdapter()).dataList.get(firstCompletePosition));
                    return;
                }
                if (firstCompletePosition == firstCompletelyVisible) return;
                firstCompletelyVisible = firstCompletePosition;
                //停止上面的和下面的player
                if (firstCompletePosition - ads.size() - 4 >= 0 && firstCompletePosition - ads.size() - 4 < players.size()) {
                    players.get(firstCompletePosition - ads.size() - 4).stop();
                    covers.get(firstCompletePosition - ads.size() - 4).setVisibility(View.VISIBLE);
                }
                if (players.size() - firstCompletePosition > 4) {
                    for (int i = players.size() - 1; i > players.size() - firstCompletePosition; i--) {
                        players.get(i).stop();
                    }
                }
                //暂停播放之前的视频
                if (playerView != null) {
                    Objects.requireNonNull(playerView.getPlayer()).pause();
                }
                View firstView = manager.findViewByPosition(firstCompletePosition);
                if (firstView != null) {
                    playerView = firstView.findViewById(R.id.player);
                    SimpleDraweeView cover = firstView.findViewById(R.id.cover);
                    if (!covers.contains(cover))
                        covers.add(cover);
                }
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
                        if (DataHolder.getInstance().recordViewed(videoDetail.getId()))
                            VideoPresenter.getInstance().recordViewed(videoDetail);
                    } else {
                        // TODO: 2020/12/8 0008 获取视频链接并播放
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        //todo:开头添加instant videos
        swipe.setOnRefreshListener(() -> swipe.setRefreshing(false));

        //连接到下载服务
        final Intent intent = new Intent(getContext(), DownloadService.class);
        Objects.requireNonNull(getContext()).bindService(intent, downloadServiceConn, Service.BIND_AUTO_CREATE);
        if (!CollectionUtils.isEmpty(DataHolder.getInstance().getPreLoadInstantVideos()))
            showData(DataHolder.getInstance().getPreLoadInstantVideos(), true);
        else VideoPresenter.getInstance().getInstantVideos(this, true);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == updateData) {
                    InstantVideoAdapter adapter = (InstantVideoAdapter) recyclerView.getAdapter();
                    if (adapter == null) return;
                    VideoDetail videoDetail = (VideoDetail) msg.obj;
                    int index = adapter.dataList.indexOf(videoDetail);
                    if (index >= 0) {
                        View content = recyclerView.getChildAt(index);
                        TextView collectNum = content.findViewById(R.id.collect_text);
                        TextView downloadNum = content.findViewById(R.id.download_text);
                        TextView likeNum = content.findViewById(R.id.like_text);
                        if (videoDetail.getCollected() > 0)
                            collectNum.setText(String.valueOf(videoDetail.getCollected()));
                        else collectNum.setText("收藏");
                        if (videoDetail.getDownloaded() > 0)
                            downloadNum.setText(String.valueOf(videoDetail.getDownloaded()));
                        else downloadNum.setText("下载");
                        if (videoDetail.getLiked() > 0)
                            likeNum.setText(String.valueOf(videoDetail.getLiked()));
                        else likeNum.setText("喜欢");
                    }
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        if (playerView != null) Objects.requireNonNull(playerView.getPlayer()).pause();
    }

    @Override
    public void onStop() {
        super.onStop();
        for (Player player : players) player.stop();
        for (SimpleDraweeView cover : covers) cover.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (playerView != null && MainActivity.getInstance().lastIndex == 2) {
            Player player = Objects.requireNonNull(playerView.getPlayer());
            player.prepare();
            player.play();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(getContext()).unbindService(downloadServiceConn);
    }

    public void showData(List<VideoDetail> videoDetails, boolean init) {
        List<Object> dataList = new ArrayList<>();
        if (init && AdManager.getInstance().size() >= 3) {
            List<UnifiedNativeAd> ads = AdManager.getInstance().nextAds(3);
            dataList.add(ads);
        }
        dataList.addAll(videoDetails);
        for (int i = 0; i < limit / 5; i++) {
            UnifiedNativeAd ad = AdManager.getInstance().nextAd();
            if (ad != null) {
                dataList.add(5 * (i + 1), ad);
            }
        }
        if (recyclerView.getAdapter() == null) {
            InstantVideoAdapter adapter = new InstantVideoAdapter((AppCompatActivity) getActivity(), dataList)
                    .setVideoClickListener(new DefaultVideoClickListener(getContext()))
                    .setDownloadListener(new DefaultDownloadClickListener(downloadService));
            recyclerView.setAdapter(adapter);
        } else {
            InstantVideoAdapter adapter = (InstantVideoAdapter) recyclerView.getAdapter();
            adapter.insert(dataList);
        }
        this.swipe.setRefreshing(false);
    }

    public StyledPlayerView getPlayerView() {
        return playerView;
    }

}
