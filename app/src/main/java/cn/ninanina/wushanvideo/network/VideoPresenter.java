package cn.ninanina.wushanvideo.network;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.PlaylistAdapter;
import cn.ninanina.wushanvideo.adapter.SingleVideoListAdapter;
import cn.ninanina.wushanvideo.adapter.VideoListAdapter;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoClickListener;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoOptionClickListener;
import cn.ninanina.wushanvideo.adapter.listener.ShowPlaylistClickListener;
import cn.ninanina.wushanvideo.model.bean.common.ResultMsg;
import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.home.SearchActivity;
import cn.ninanina.wushanvideo.ui.home.VideoListFragment;
import cn.ninanina.wushanvideo.ui.me.MeFragment;
import cn.ninanina.wushanvideo.ui.video.DetailFragment;
import cn.ninanina.wushanvideo.ui.video.PlaylistActivity;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;
import cn.ninanina.wushanvideo.util.DialogManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 处理一切关于视频的网络请求，包括推荐视频、相关视频、搜索视频、获取视频链接、收藏视频等
 */
public class VideoPresenter extends BasePresenter {
    private static final VideoPresenter presenter = new VideoPresenter();

    private VideoPresenter() {
    }

    public static VideoPresenter getInstance() {
        return presenter;
    }

    public void getRecommendVideoList(VideoListFragment fragment, RecyclerViewOp recyclerViewOp) {
        fragment.setLoading(true);
        RecyclerView recyclerView = fragment.getRecyclerView();
        SwipeRefreshLayout swipeRefreshLayout = fragment.getSwipeRefreshLayout();
        String type = fragment.getType();
        getVideoService().getRecommend(getAppKey(), type, 10, getToken())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(recyclerView.getContext(), "网络开小差了~", Toast.LENGTH_SHORT).show();
                    //TODO:如果空白，点击刷新
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResult -> {
                    List<VideoDetail> videoDetails = listResult.getData();
                    //1个广告+3个视频（开头也是广告）
                    List<Object> dataList = new ArrayList<>(videoDetails);
                    switch (recyclerViewOp) {
                        case INIT:
                            VideoListAdapter adapter = new VideoListAdapter(dataList,
                                    new DefaultVideoClickListener(fragment.getContext()), new DefaultVideoOptionClickListener(fragment.getContext()));
                            recyclerView.setAdapter(adapter);
                            break;
                        case SWIPE:
                            adapter = (VideoListAdapter) recyclerView.getAdapter();
                            assert adapter != null;
                            adapter.insertToStart(dataList);
                            recyclerView.scrollToPosition(0);
                            break;
                        case APPEND:
                            adapter = (VideoListAdapter) recyclerView.getAdapter();
                            assert adapter != null;
                            adapter.append(dataList);
                            break;
                        default:
                    }
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                    fragment.setLoading(false);
                });
    }

    public void getRelatedVideos(DetailFragment fragment, long videoId) {
        String appKey = WushanApp.getAppKey();
        RecyclerView recyclerView = fragment.getRelatedRecyclerView();
        getVideoService().getRelatedVideos(appKey, videoId, 0, 10)
                .subscribeOn(Schedulers.io())
                .timeout(5, TimeUnit.SECONDS)
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(fragment.getContext(), "网络开小差了~", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResult -> {
                    List<VideoDetail> videoDetails = listResult.getData();
                    fragment.getDataList().addAll(videoDetails);
                    recyclerView.setAdapter(new SingleVideoListAdapter(fragment.getDataList(),
                            new DefaultVideoClickListener(fragment.getContext()), new DefaultVideoOptionClickListener(fragment.getContext())));
                });
    }

    public void searchForVideo(SearchActivity searchActivity, String query, int offset, int limit) {
        RecyclerView recyclerView = searchActivity.getRecyclerView();
        String appKey = WushanApp.getAppKey();
        getVideoService().search(appKey, query, offset, limit)
                .subscribeOn(Schedulers.io())
                .timeout(5, TimeUnit.SECONDS)
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(searchActivity, "网络开小差了~", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResult -> {
                    List<Object> dataList = new ArrayList<>(listResult.getData());
                    recyclerView.setAdapter(new VideoListAdapter(dataList, new DefaultVideoClickListener(searchActivity), new DefaultVideoOptionClickListener(searchActivity)));
                });
    }

    public void getSrc(VideoDetailActivity videoDetailActivity, long id) {
        getVideoService().getVideoDetail(WushanApp.getAppKey(), id, getToken())
                .subscribeOn(Schedulers.io())
                .timeout(20, TimeUnit.SECONDS)
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(videoDetailActivity, "网络开小差了~", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videoDetailResult -> {
                    VideoDetail detail = videoDetailResult.getData();
                    if (VideoListAdapter.isSrcValid(detail.getSrc()))
                        videoDetailActivity.startPlaying(detail.getSrc());
                    else
                        Toast.makeText(videoDetailActivity, "视频已经被原作者删除了，抱歉！", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * 获取播单，并展示在对话框中，用于收藏视频
     */
    public void getPlaylistForDialog(Context context, VideoDetail videoDetail) {
        getVideoService().getPlaylist(getAppKey(), getToken())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(context, "网络开小差了~", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResult -> {
                    DialogManager.getInstance().newCollectDialog(context, videoDetail, listResult.getData()).show();
                });
    }

    /**
     * 获取视频收藏夹，并展示在MeFragment中
     */
    public void getPlaylistForMe(MeFragment meFragment) {
        getVideoService().getPlaylist(getAppKey(), getToken())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(meFragment.getContext(), "网络开小差了~", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResult -> {
                    RecyclerView playlist = meFragment.getPlaylist();
                    if (!CollectionUtils.isEmpty(listResult.getData())) {
                        playlist.setAdapter(new PlaylistAdapter(listResult.getData(), new ShowPlaylistClickListener(meFragment.getContext())));
                        meFragment.setData(listResult.getData());
                    }
                });
    }

    /**
     * 新建收藏夹，创建完成后加入给定的recyclerView中
     */
    public void createPlaylist(Context context, RecyclerView recyclerView, String name) {
        getVideoService().createPlaylist(getAppKey(), name, getToken())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(context, "网络开小差了~", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dirResult -> {
                    PlaylistAdapter adapter = (PlaylistAdapter) recyclerView.getAdapter();
                    if (adapter == null) {
                        List<Playlist> list = new ArrayList<>();
                        list.add(dirResult.getData());
                        recyclerView.setAdapter(new PlaylistAdapter(list, new ShowPlaylistClickListener(context)));
                    } else {
                        adapter.insert(dirResult.getData());
                        adapter.notifyDataSetChanged();
                    }
                    Toast.makeText(context, "创建成功", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * 收藏视频
     */
    public void collectVideo(Context context, long videoId, long playlistId) {
        getVideoService().collectVideo(getAppKey(), videoId, playlistId, getToken())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(context, "网络出了点问题...", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    String code = result.getRspCode();
                    if (code.equals(ResultMsg.NOT_LOGIN.getCode())) {
                        Toast.makeText(context, "会话已过期，请刷新一下", Toast.LENGTH_SHORT).show();
                    } else if (code.equals(ResultMsg.INVALID_VIDEO_ID.getCode()) || code.equals(ResultMsg.COLLECT_WRONG_DIR.getCode())) {
                        Toast.makeText(context, "出了点问题...", Toast.LENGTH_SHORT).show();
                    } else if (code.equals(ResultMsg.COLLECT_ALREADY.getCode())) {
                        Toast.makeText(context, "已经收藏过了", Toast.LENGTH_SHORT).show();
                    } else if (code.equals(ResultMsg.SUCCESS.getCode())) {
                        Toast.makeText(context, "收藏成功！", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 展示播单的视频列表
     */
    public void getVideosForPlaylist(PlaylistActivity playlistActivity, long playlistId) {
        getVideoService().getPlaylistVideos(getAppKey(), playlistId)
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(playlistActivity, "网络出了点问题...", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResult -> {
                    RecyclerView recyclerView = playlistActivity.getContent();
                    List<Object> dataList = new ArrayList<>(listResult.getData());
                    recyclerView.setAdapter(new SingleVideoListAdapter(dataList,
                            new DefaultVideoClickListener(playlistActivity), new DefaultVideoOptionClickListener(playlistActivity)));
                });
    }

    public enum RecyclerViewOp {
        INIT, //第一次填充
        SWIPE, //下拉刷新
        APPEND, //下滑添加
    }

}
