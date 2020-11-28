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
import cn.ninanina.wushanvideo.adapter.CommentAdapter;
import cn.ninanina.wushanvideo.adapter.PlaylistAdapter;
import cn.ninanina.wushanvideo.adapter.SingleVideoListAdapter;
import cn.ninanina.wushanvideo.adapter.VideoListAdapter;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoClickListener;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoOptionClickListener;
import cn.ninanina.wushanvideo.adapter.listener.ReplyCommentListener;
import cn.ninanina.wushanvideo.adapter.listener.ShowPlaylistClickListener;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.common.ResultMsg;
import cn.ninanina.wushanvideo.model.bean.video.Comment;
import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.ui.home.SearchActivity;
import cn.ninanina.wushanvideo.ui.home.VideoListFragment;
import cn.ninanina.wushanvideo.ui.video.CommentFragment;
import cn.ninanina.wushanvideo.ui.video.DetailFragment;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;
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
                    fragment.setLoading(false);
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
                .subscribe(playlistResult -> {
                    PlaylistAdapter adapter = (PlaylistAdapter) recyclerView.getAdapter();
                    if (adapter == null) {
                        List<Playlist> list = new ArrayList<>();
                        list.add(playlistResult.getData());
                        recyclerView.setAdapter(new PlaylistAdapter(list, new ShowPlaylistClickListener(context)));
                    } else {
                        adapter.insert(playlistResult.getData());
                        adapter.notifyDataSetChanged();
                    }
                    if (CollectionUtils.isEmpty(DataHolder.getInstance().getPlaylists())) {
                        DataHolder.getInstance().setPlaylists(new ArrayList<>());
                    }
                    DataHolder.getInstance().getPlaylists().add(playlistResult.getData());
                    Toast.makeText(context, "创建成功", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * 收藏视频
     */
    public void collectVideo(Context context, VideoDetail videoDetail, Playlist playlist) {
        getVideoService().collectVideo(getAppKey(), videoDetail.getId(), playlist.getId(), getToken())
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
                        playlist.getVideoDetails().add(videoDetail);
                        playlist.setUpdateTime(System.currentTimeMillis());
                        playlist.setCover(videoDetail.getCoverUrl());
                        playlist.setCount(playlist.getCount() + 1);
                        if (MainActivity.getInstance().getDetailFragment() != null) {
                            MainActivity.getInstance().getDetailFragment().refreshCollect();
                        }
                    }
                });
    }

    /**
     * 获取用户的所有播单以及包含的视频，程序启动时就调用
     */
    public void loadPlaylists() {
        getVideoService().getPlaylist(getAppKey(), getToken())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(WushanApp.getInstance(), "网络开小差了~", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResult -> {
                    DataHolder.getInstance().setPlaylists(listResult.getData());
                    loadPlaylistVideos();
                    if (MainActivity.getInstance().getMeFragment() != null)
                        MainActivity.getInstance().getMeFragment().refresh();
                });
    }

    /**
     * 获取用户所有播单里面的所有视频
     */
    private void loadPlaylistVideos() {
        for (Playlist playlist : DataHolder.getInstance().getPlaylists()) {
            long playlistId = playlist.getId();
            getVideoService().getPlaylistVideos(getAppKey(), playlistId)
                    .subscribeOn(Schedulers.io())
                    .doOnError(throwable -> {
                        Looper.prepare();
                        Toast.makeText(WushanApp.getInstance(), "网络出了点问题...", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listResult -> {
                        playlist.setVideoDetails(listResult.getData());
                    });
        }
    }

    public void loadComments(CommentFragment commentFragment) {
        commentFragment.setLoading(true);
        getVideoService().getComments(getAppKey(), commentFragment.getVideoDetail().getId(), commentFragment.getPage(), 20, commentFragment.getSort().getCode())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(commentFragment.getContext(), "网络出了点问题...", Toast.LENGTH_SHORT).show();
                    commentFragment.setLoading(false);
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    List<Comment> comments = result.getData();
                    if (CollectionUtils.isEmpty(comments)) {
                        if (commentFragment.getPage() == 0) {
                            //TODO:处理空评论
                        } else {
                            commentFragment.setLoadComplete(true);
                            Toast.makeText(commentFragment.getContext(), "没有更多啦~", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                    RecyclerView recyclerView = commentFragment.getRecyclerView();
                    if (recyclerView.getAdapter() == null) {
                        recyclerView.setAdapter(new CommentAdapter(comments, new ReplyCommentListener()));
                    } else {
                        CommentAdapter adapter = (CommentAdapter) recyclerView.getAdapter();
                        adapter.insert(comments);
                    }
                    commentFragment.setLoading(false);
                });
    }

    public void publishComment(CommentFragment commentFragment) {
        getVideoService().commentOn(getAppKey(), commentFragment.getVideoDetail().getId(), commentFragment.getInput().getText().toString(), getToken(), null)
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(commentFragment.getContext(), "网络出了点问题...", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Toast.makeText(commentFragment.getContext(), "评论成功！", Toast.LENGTH_SHORT).show();
                    commentFragment.getInput().setText("");
                    Comment comment = result.getData();
                    RecyclerView recyclerView = commentFragment.getRecyclerView();
                    if (recyclerView.getAdapter() == null) {
                        recyclerView.setAdapter(new CommentAdapter(new ArrayList<Comment>() {{
                            add(comment);
                        }}, new ReplyCommentListener()));
                    } else {
                        CommentAdapter adapter = (CommentAdapter) recyclerView.getAdapter();
                        adapter.insert(comment);
                    }
                });
    }

    public void approveComment(Comment comment) {
        getVideoService().approveComment(getAppKey(), comment.getId(), getToken())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(WushanApp.getInstance(), "网络出了点问题...", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                });
    }

    public void disapproveComment(Comment comment) {
        getVideoService().disapproveComment(getAppKey(), comment.getId(), getToken())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(WushanApp.getInstance(), "网络出了点问题...", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                });
    }

    public enum RecyclerViewOp {
        INIT, //第一次填充
        SWIPE, //下拉刷新
        APPEND, //下滑添加
    }

}
