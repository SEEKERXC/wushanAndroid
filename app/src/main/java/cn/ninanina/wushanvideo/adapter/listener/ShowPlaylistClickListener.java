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
        intent.putExtra("id", playlist.getId().longValue());
        intent.putExtra("name", playlist.getName());
        intent.putExtra("count", playlist.getCount().intValue());
        intent.putExtra("cover", playlist.getCover());
        intent.putExtra("public", playlist.getIsPublic().booleanValue());
        intent.putExtra("update", playlist.getUpdateTime().longValue());
        intent.putExtra("create", playlist.getCreateTime().longValue());
        intent.putExtra("userId", playlist.getUser().getId().longValue());
        intent.putExtra("username", playlist.getUser().getNickname());
        intent.putExtra("userPhoto", playlist.getUser().getPhoto());
        context.startActivity(intent);
    }

}
