package cn.ninanina.wushanvideo.network;

import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import cn.ninanina.wushanvideo.WushanApp;
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
                .timeout(15, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> Toast.makeText(videoDetailActivity, "出了点问题，请稍后再试哦！", Toast.LENGTH_SHORT).show())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videoDetailResult -> {
                    VideoDetail detail = videoDetailResult.getData();
                    videoDetailActivity.startPlaying(detail.getSrc());
                });
    }
}
