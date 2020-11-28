package cn.ninanina.wushanvideo.model;

import java.util.List;

import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import lombok.Data;

/**
 * 用于临时保存用户除了profile之外的所有数据。
 */
@Data
public class DataHolder {
    private static DataHolder instance = new DataHolder();

    private DataHolder() {
    }

    public static DataHolder getInstance() {
        return instance;
    }

    //用户的播单列表
    List<Playlist> playlists;

    public Playlist getPlaylistVideos(long id) {
        for (Playlist playlist : playlists) {
            if (playlist.getId().equals(id)) return playlist;
        }
        return null;
    }

    /**
     * 查看是否有歌单包含video
     */
    public boolean collectedVideo(long videoId) {
        for (Playlist playlist : playlists) {
            for (VideoDetail videoDetail : playlist.getVideoDetails()) {
                if (videoDetail.getId().equals(videoId)) return true;
            }
        }
        return false;
    }
}
