package cn.ninanina.wushanvideo.model;

import java.util.ArrayList;
import java.util.List;

import cn.ninanina.wushanvideo.WushanApp;
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
        playlists = new ArrayList<>();
        likedVideos = new ArrayList<>();
        dislikedVideos = new ArrayList<>();
    }

    public static DataHolder getInstance() {
        return instance;
    }

    //用户的播单列表
    List<Playlist> playlists;
    //用户所有喜欢了的视频id
    List<Long> likedVideos;
    //用户所有不喜欢的视频id
    List<Long> dislikedVideos;

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
        if (!WushanApp.loggedIn()) return false;
        for (Playlist playlist : playlists) {
            for (VideoDetail videoDetail : playlist.getVideoDetails()) {
                if (videoDetail.getId().equals(videoId)) return true;
            }
        }
        return false;
    }

    /**
     * 查看是否喜欢video
     */
    public boolean likedVideo(long videoId) {
        return likedVideos.contains(videoId);
    }

    /**
     * 查看是否不喜欢video
     */
    public boolean dislikedVideo(long videoId) {
        return dislikedVideos.contains(videoId);
    }
}
