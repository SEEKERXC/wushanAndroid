package cn.ninanina.wushanvideo.network;

import android.os.Looper;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.VideoListAdapter;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.home.SearchActivity;
import cn.ninanina.wushanvideo.ui.home.VideoListFragment;
import cn.ninanina.wushanvideo.ui.video.DetailFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class VideoListPresenter extends BasePresenter {
    private static final VideoListPresenter presenter = new VideoListPresenter();

    private VideoListPresenter() {
    }

    public static VideoListPresenter getInstance() {
        return presenter;
    }

    public void getVideoList(VideoListFragment fragment, SwipeRefreshLayout swipeRefreshLayout, Op op, String type) {
        RecyclerView recyclerView = fragment.getRecyclerView();
        VideoListAdapter.ItemClickListener listener = fragment.getClickListener();
        getVideoService().getRecommend(WushanApp.getAppKey(), type, 10)
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(recyclerView.getContext(), "出了点问题，请稍后刷新试试！", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResult -> {
                    List<VideoDetail> videoDetails = listResult.getData();
                    //1个广告+3个视频（开头也是广告）
                    List<Object> dataList = new ArrayList<>(videoDetails);
                    switch (op) {
                        case INIT:
                            VideoListAdapter adapter = new VideoListAdapter(dataList, listener);
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
                });
    }

    public void getRelatedVideos(RecyclerView recyclerView, long id) {
        String appKey = WushanApp.getAppKey();
        getVideoService().getRelatedVideos(appKey, id)
                .subscribeOn(Schedulers.io())
                .timeout(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResult -> {
                    List<VideoDetail> videoDetails = listResult.getData();
                    List<Object> dataList = new ArrayList<>();
                    //1个广告+3个视频（开头也是广告）
                    for (int i = 0; i < videoDetails.size(); i++) {
                        dataList.add(videoDetails.get(i));
                    }

                });
    }

    public void searchForVideo(SearchActivity searchActivity, String query, int offset, int limit) {
        RecyclerView recyclerView = searchActivity.getRecyclerView();
        VideoListAdapter.ItemClickListener listener = searchActivity.getClickListener();
        String appKey = WushanApp.getAppKey();
        getVideoService().search(appKey, query, offset, limit)
                .subscribeOn(Schedulers.io())
                .timeout(5, TimeUnit.SECONDS)
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(searchActivity, "网络出了点问题...", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResult -> {
                    List<Object> dataList = new ArrayList<>(listResult.getData());
                    recyclerView.setAdapter(new VideoListAdapter(dataList, listener));
                });
    }

    public enum Op {
        INIT, //第一次填充
        SWIPE, //下拉刷新
        APPEND, //下滑添加
    }

}
