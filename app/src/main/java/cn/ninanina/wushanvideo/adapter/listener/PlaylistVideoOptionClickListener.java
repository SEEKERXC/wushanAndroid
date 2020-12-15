package cn.ninanina.wushanvideo.adapter.listener;

import android.app.Activity;
import android.content.Context;

import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.util.DialogManager;

public class PlaylistVideoOptionClickListener implements VideoClickListener {

    public PlaylistVideoOptionClickListener(Activity activity, Playlist playlist) {
        this.activity = activity;
        this.playlist = playlist;
    }

    private Activity activity;
    private Playlist playlist;

    @Override
    public void onClick(VideoDetail videoDetail) {
        DialogManager.getInstance().newPlaylistVideoOptionDialog(activity, videoDetail, playlist).show();
    }
}
