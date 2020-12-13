package cn.ninanina.wushanvideo.adapter.listener;

import android.content.Context;
import android.content.Intent;

import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.ui.video.PlaylistActivity;

/**
 * 点击播单进入播单详情页面
 */
public class ShowPlaylistClickListener implements PlaylistClickListener {
    public ShowPlaylistClickListener(Context context) {
        this.context = context;
    }

    private Context context;

    @Override
    public void onPlaylistClicked(Playlist playlist) {
        Intent intent = new Intent(context, PlaylistActivity.class);
        intent.putExtra("playlist", playlist);
        context.startActivity(intent);
    }

}
