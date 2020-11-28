package cn.ninanina.wushanvideo.adapter.listener;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;

/**
 * 点击播单收藏视频
 */
public class CollectPlaylistClickListener implements PlaylistClickListener {
    private Context context;
    private VideoDetail videoDetail;
    private AlertDialog dialog;

    public CollectPlaylistClickListener(Context context, VideoDetail videoDetail, AlertDialog dialog) {
        this.context = context;
        this.videoDetail = videoDetail;
        this.dialog = dialog;
    }

    @Override
    public void onPlaylistClicked(Playlist playlist) {
        VideoPresenter.getInstance().collectVideo(context, videoDetail, playlist);
        dialog.dismiss();
    }
}
