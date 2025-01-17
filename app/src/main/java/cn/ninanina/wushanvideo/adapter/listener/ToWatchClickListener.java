package cn.ninanina.wushanvideo.adapter.listener;

import cn.ninanina.wushanvideo.model.bean.common.Pair;
import cn.ninanina.wushanvideo.model.bean.video.ToWatch;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.home.WatchLaterActivity;
import cn.ninanina.wushanvideo.util.DialogManager;

public class ToWatchClickListener {
    public ToWatchClickListener(WatchLaterActivity activity) {
        this.activity = activity;
    }

    private WatchLaterActivity activity;

    public void onClicked(Pair<ToWatch, VideoDetail> pair) {
        DialogManager.getInstance().newWatchLaterOptionDialog(activity, pair).show();
    }
}
