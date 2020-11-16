package cn.ninanina.wushanvideo.network;

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
import cn.ninanina.wushanvideo.ui.home.VideoListFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class VideoListPresenter extends BasePresenter {
    private static final VideoListPresenter presenter = new VideoListPresenter();

    private VideoListPresenter() {
    }

    public static VideoListPresenter getInstance() {
        return presenter;
    }

    public void getVideoList(RecyclerView recyclerView, SwipeRefreshLayout swipeRefreshLayout, Op op) {
        getVideoService().getRecommend(WushanApp.getAppKey(), 10)
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> Toast.makeText(recyclerView.getContext(), "出了点问题，请稍后刷新试试！", Toast.LENGTH_SHORT).show())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResult -> {
                    List<VideoDetail> videoDetails = listResult.getData();
                    List<Object> dataList = new ArrayList<>();
                    //1个广告+3个视频（开头也是广告）
                    for (int i = 0; i < videoDetails.size(); i++) {
                        if (dataList.size() % VideoListAdapter.ITEMS_PER_AD == 0) {
                            AdView adView = new AdView(recyclerView.getContext());
                            adView.setAdSize(AdSize.BANNER);
                            adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
                            dataList.add(adView);
                        }
                        dataList.add(videoDetails.get(i));
                    }
                    switch (op) {
                        case INIT:
                            VideoListAdapter adapter = new VideoListAdapter(dataList, VideoListFragment.getClickListener());
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
                        if (dataList.size() % VideoListAdapter.ITEMS_PER_AD == 0) {
                            AdView adView = new AdView(recyclerView.getContext());
                            adView.setAdSize(AdSize.BANNER);
                            adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
                            dataList.add(adView);
                        }
                        dataList.add(videoDetails.get(i));
                    }
                    recyclerView.setAdapter(new VideoListAdapter(dataList, VideoListFragment.getClickListener()));
                });
    }

    public enum Op {
        INIT, //第一次填充
        SWIPE, //下拉刷新
        APPEND, //下滑添加
    }

}
