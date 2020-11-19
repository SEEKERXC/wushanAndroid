package cn.ninanina.wushanvideo.network;

import android.os.Looper;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.VideoListAdapter;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class VideoDetailPresenter extends BasePresenter {
    private VideoDetailActivity videoDetailActivity;

    public VideoDetailPresenter(VideoDetailActivity videoDetailActivity) {
        this.videoDetailActivity = videoDetailActivity;
    }

    public void access(long id) {
        getVideoService().getVideoDetail(WushanApp.getAppKey(), id)
                .timeout(20, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(videoDetailActivity, "出了点问题，请稍后再试哦！", Toast.LENGTH_SHORT).show();
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
}
