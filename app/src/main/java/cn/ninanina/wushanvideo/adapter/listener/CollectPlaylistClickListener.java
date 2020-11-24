package cn.ninanina.wushanvideo.adapter.listener;

import android.content.Context;

import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;

/**
 * 点击播单收藏视频
 */
public class CollectPlaylistClickListener implements PlaylistClickListener {
    private Context context;
    private VideoDetail videoDetail;

    public CollectPlaylistClickListener(Context context, VideoDetail videoDetail) {
        this.context = context;
        this.videoDetail = videoDetail;
    }

    @Override
    public void onPlaylistClicked(Playlist playlist) {
        VideoPresenter.getInstance().collectVideo(context, videoDetail.getId(), playlist.getId());
    }
}
