package cn.ninanina.wushanvideo.network;

import java.util.List;

import cn.ninanina.wushanvideo.model.bean.Result;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.home.VideoListFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class VideoListPresenter extends BasePresenter{

    private VideoListFragment fragment;

    public VideoListPresenter(VideoListFragment fragment) {
        this.fragment = fragment;
    }

    public void getVideoList() {
        getVideoService().getRecommend("xiaofei")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Result<List<VideoDetail>>>() {
                    @Override
                    public void accept(Result<List<VideoDetail>> listResult) {
                        fragment.setItems(listResult.getData());
                    }
                });
    }
}
