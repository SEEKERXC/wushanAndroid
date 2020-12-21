package cn.ninanina.wushanvideo.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.model.bean.video.VideoUserViewed;
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
        allViewed = new ArrayList<>();
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
    //用户所有的浏览记录
    List<VideoUserViewed> allViewed;
    //预加载instant videos
    List<VideoDetail> preLoadInstantVideos;

    public Playlist getPlaylistVideos(long id) {
        for (Playlist playlist : playlists) {
            if (playlist.getId().equals(id)) return playlist;
        }
        return null;
    }

    public void updatePlaylist(Playlist playlist) {
        playlists.remove(playlist);
        playlists.add(playlist);
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

    /**
     * 本地增加一条观看记录，用以实时判断是否看过，如果此次记录距离上次观看不到6小时，不做记录，返回是否记录
     */
    public boolean recordViewed(long videoId) {
        if (!WushanApp.loggedIn()) return false;
        for (VideoUserViewed viewed : allViewed) {
            if (viewed.getVideoId() == videoId) {
                if (System.currentTimeMillis() - viewed.getTime() < 6 * 3600 * 1000)
                    return false;
                viewed.setViewCount(viewed.getViewCount() + 1);
                viewed.setTime(System.currentTimeMillis());
                viewed.setViewCount(viewed.getViewCount() + 1);
                return true;
            }
        }
        VideoUserViewed viewed = new VideoUserViewed();
        viewed.setVideoId(videoId);
        viewed.setViewCount(1);
        viewed.setTime(System.currentTimeMillis());
        allViewed.add(viewed);
        return true;
    }

    /**
     * 获取指定视频看过的次数。未看过返回0
     */
    public int viewedCount(long videoId) {
        for (VideoUserViewed viewed : allViewed) {
            if (viewed.getVideoId() == videoId) return viewed.getViewCount();
        }
        return 0;
    }
}
