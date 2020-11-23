package cn.ninanina.wushanvideo.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.orhanobut.dialogplus.DialogPlus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.CollectItemAdapter;
import cn.ninanina.wushanvideo.adapter.SingleVideoListAdapter;
import cn.ninanina.wushanvideo.adapter.VideoListAdapter;
import cn.ninanina.wushanvideo.model.bean.common.ResultMsg;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.model.bean.video.VideoDir;
import cn.ninanina.wushanvideo.ui.home.SearchActivity;
import cn.ninanina.wushanvideo.ui.home.VideoListFragment;
import cn.ninanina.wushanvideo.ui.me.MeFragment;
import cn.ninanina.wushanvideo.ui.video.DetailFragment;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;
import cn.ninanina.wushanvideo.util.DialogManager;
import cn.ninanina.wushanvideo.util.LayoutParamsUtil;
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
        VideoListAdapter.ItemClickListener listener = fragment.getClickListener();
        VideoListAdapter.OptionsClickListener clickListener = fragment.getOptionsClickListener();
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
                            VideoListAdapter adapter = new VideoListAdapter(dataList, listener, clickListener);
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
                    recyclerView.setAdapter(new SingleVideoListAdapter(fragment.getDataList(), fragment.getItemClickListener(), fragment.getOptionsClickListener()));
                });
    }

    public void searchForVideo(SearchActivity searchActivity, String query, int offset, int limit) {
        RecyclerView recyclerView = searchActivity.getRecyclerView();
        VideoListAdapter.ItemClickListener listener = searchActivity.getClickListener();
        VideoListAdapter.OptionsClickListener clickListener = searchActivity.getOptionsClickListener();
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
                    recyclerView.setAdapter(new VideoListAdapter(dataList, listener, clickListener));
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
     * 获取视频收藏夹，并展示在对话框中，用于收藏视频
     */
    public void getVideoDirsForDialog(Context context, VideoDetail videoDetail) {
        getVideoService().collectList(getAppKey(), getToken())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(context, "网络开小差了~", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResult -> {
                    DialogManager.getInstance().newCollectDialog(context, videoDetail, listResult.getData());
                    Message message = new Message();
                    message.what = DialogManager.MessageType.COLLECT_DIALOG_FINISHED;
                    DialogManager.getInstance().handler.sendMessage(message);
                });
    }

    /**
     * 获取视频收藏夹，并展示在MeFragment中
     */
    public void getVideoDirsForMe(MeFragment meFragment) {
        getVideoService().collectList(getAppKey(), getToken())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(meFragment.getContext(), "网络开小差了~", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResult -> {
                    ListView listView = meFragment.getCollectList();
                    listView.setAdapter(new CollectItemAdapter(meFragment.getContext(), R.layout.item_collect_dir, listResult.getData()));
                    LayoutParamsUtil.adaptCollectListViewHeight(listView);
                });
    }

    /**
     * 新建收藏夹
     */
    public void createVideoDir(Context context, ListView listView, String name) {
        getVideoService().createVideoDir(getAppKey(), name, getToken())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(context, "网络开小差了~", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dirResult -> {
                    CollectItemAdapter adapter = (CollectItemAdapter) listView.getAdapter();
                    adapter.insert(dirResult.getData(), 0);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(context, "创建成功", Toast.LENGTH_SHORT).show();
                    LayoutParamsUtil.adaptCollectListViewHeight(listView);
                });
    }

    /**
     * 收藏视频
     */
    public void collectVideo(Context context, long videoId, long dirId) {
        getVideoService().collectVideo(getAppKey(), videoId, dirId, getToken())
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

    public enum RecyclerViewOp {
        INIT, //第一次填充
        SWIPE, //下拉刷新
        APPEND, //下滑添加
    }

}
