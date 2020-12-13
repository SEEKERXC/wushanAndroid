package cn.ninanina.wushanvideo.adapter.listener;

import android.content.Context;

import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.util.DialogManager;

public class PlaylistVideoOptionClickListener implements VideoClickListener {

    public PlaylistVideoOptionClickListener(Context context, Playlist playlist) {
        this.context = context;
        this.playlist = playlist;
    }

    private Context context;
    private Playlist playlist;

    @Override
    public void onClick(VideoDetail videoDetail) {
        DialogManager.getInstance().newPlaylistVideoOptionDialog(context, videoDetail, playlist).show();
    }
}
