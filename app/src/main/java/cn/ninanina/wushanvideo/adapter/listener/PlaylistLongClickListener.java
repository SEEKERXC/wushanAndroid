package cn.ninanina.wushanvideo.adapter.listener;

import android.content.Context;

import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.util.DialogManager;

public class PlaylistLongClickListener implements PlaylistClickListener {
    public PlaylistLongClickListener(Context context) {
        this.context = context;
    }

    Context context;

    @Override
    public void onPlaylistClicked(Playlist playlist) {
        DialogManager.getInstance().newPlaylistOptionDialog(context, playlist).show();
    }
}
