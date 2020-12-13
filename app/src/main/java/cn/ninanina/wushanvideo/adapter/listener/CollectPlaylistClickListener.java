package cn.ninanina.wushanvideo.adapter.listener;

import android.app.Dialog;
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
    private VideoDetail videoDetail;
    private Dialog dialog;

    public CollectPlaylistClickListener(VideoDetail videoDetail, Dialog dialog) {
        this.videoDetail = videoDetail;
        this.dialog = dialog;
    }

    @Override
    public void onPlaylistClicked(Playlist playlist) {
        VideoPresenter.getInstance().collectVideo(videoDetail, playlist);
        dialog.dismiss();
    }
}
