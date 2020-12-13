package cn.ninanina.wushanvideo.adapter.listener;

import cn.ninanina.wushanvideo.model.bean.common.Pair;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.model.bean.video.VideoUserViewed;
import cn.ninanina.wushanvideo.ui.home.HistoryActivity;
import cn.ninanina.wushanvideo.util.DialogManager;

public class HistoryClickListener {
    public HistoryClickListener(HistoryActivity activity) {
        this.activity = activity;
    }

    private HistoryActivity activity;

    public void onClicked(Pair<VideoUserViewed, VideoDetail> pair) {
        DialogManager.getInstance().newHistoryOptionDialog(activity, pair).show();
    }
}
