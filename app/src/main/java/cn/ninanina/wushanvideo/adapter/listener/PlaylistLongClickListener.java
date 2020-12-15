package cn.ninanina.wushanvideo.adapter.listener;

import android.app.Activity;
import android.content.Context;

import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.util.DialogManager;

public class PlaylistLongClickListener implements PlaylistClickListener {
    public PlaylistLongClickListener(Activity activity) {
        this.activity = activity;
    }

    Activity activity;

    @Override
    public void onPlaylistClicked(Playlist playlist) {
        DialogManager.getInstance().newPlaylistOptionDialog(activity, playlist).show();
    }
}
