package cn.ninanina.wushanvideo.network;

import android.widget.Toast;

import cn.ninanina.wushanvideo.model.bean.Result;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class VideoDetailPresenter extends BasePresenter {
    private VideoDetailActivity videoDetailActivity;

    public VideoDetailPresenter(VideoDetailActivity videoDetailActivity) {
        this.videoDetailActivity = videoDetailActivity;
    }

    public void access(long id) {
        getVideoService().getVideoDetail(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(videoDetailActivity, "出了点问题，请稍后再试哦！", Toast.LENGTH_SHORT).show();
                    }
                })
                .subscribe(new Consumer<Result<VideoDetail>>() {
                    @Override
                    public void accept(Result<VideoDetail> videoDetailResult) {
                        VideoDetail detail = videoDetailResult.getData();
                        videoDetailActivity.startPlaying(detail.getSrc());
                    }
                });
    }
}
