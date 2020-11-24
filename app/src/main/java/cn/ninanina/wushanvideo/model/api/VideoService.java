package cn.ninanina.wushanvideo.model.api;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import cn.ninanina.wushanvideo.model.bean.Result;
import cn.ninanina.wushanvideo.model.bean.video.Comment;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 所有关于视频的API都在此
 */
public interface VideoService {
    //获取推荐视频
    @GET("video/recommend")
    Observable<Result<List<VideoDetail>>> getRecommend(@Query("appKey") String appKey,
                                                       @Query("type") String type,
                                                       @Query("limit") int limit,
                                                       @Query("token") String token);

    //获取视频链接
    @GET("video/detail")
    Observable<Result<VideoDetail>> getVideoDetail(@Query("appKey") String appKey,
                                                   @Query("id") long id,
                                                   @Query("token") String token);

    //获取相关视频
    @GET("video/related")
    Observable<Result<List<VideoDetail>>> getRelatedVideos(@Query("appKey") String appKey,
                                                           @Query("id") long id,
                                                           @Query("offset") int offset,
                                                           @Query("limit") int limit);

    //获取当前观影人数
    @GET("video/audience")
    Observable<Result<Integer>> getAudienceNum(@Query("id") long id);

    //退出播放器调用
    @POST("video/exit")
    Observable<Result<ObjectUtils.Null>> exitVideoPlayer(@Query("id") long id);

    //获取在线视频排行
    @GET("video/rank/online")
    Observable<Result<Pair<VideoDetail, Integer>>> onlineRank(@Query("limit") int limit);

    //发表评论
    @POST("video/comment")
    Observable<Result<Comment>> commentOn(@Query("id") long id,
                                          @Query("content") String content,
                                          @Query("parentId") Long parentId,
                                          @Query("token") String token);

    //创建播单
    @POST("video/playlist/create")
    Observable<Result<Playlist>> createPlaylist(@Query("appKey") String appKey,
                                                @Query("name") String name,
                                                @Query("token") String token);

    //删除播单
    @POST("video/playlist/delete")
    Observable<Result<ObjectUtils.Null>> deletePlaylist(@Query("appKey") String appKey,
                                                        @Query("dirId") Long dirId,
                                                        @Query("token") String token);

    //重命名播单
    @POST("video/playlist/rename")
    Observable<Result<Playlist>> renamePlaylist(@Query("appKey") String appKey,
                                                @Query("dirId") Long dirId,
                                                @Query("name") String name,
                                                @Query("token") String token);

    //获取播单列表
    @GET("video/playlist")
    Observable<Result<List<Playlist>>> getPlaylist(@Query("appKey") String appKey,
                                                   @Query("token") String token);

    //获取播单的视频列表
    @GET("video/playlist/videos")
    Observable<Result<List<VideoDetail>>> getPlaylistVideos(@Query("appKey") String appKey,
                                                            @Query("id") Long id);

    //收藏视频
    @POST("video/collect")
    Observable<Result<ObjectUtils.Null>> collectVideo(@Query("appKey") String appKey,
                                                      @Query("videoId") Long videoId,
                                                      @Query("dirId") Long dirId,
                                                      @Query("token") String token);

    //取消收藏视频
    @POST("video/collect/cancel")
    Observable<Result<ObjectUtils.Null>> cancelCollect(@Query("appKey") String appKey,
                                                       @Query("videoId") Long videoId,
                                                       @Query("dirId") Long dirId,
                                                       @Query("token") String token);

    //搜索视频
    @GET("video/search")
    Observable<Result<List<VideoDetail>>> search(@Query("appKey") String appKey,
                                                 @Query("query") String query,
                                                 @Query("offset") Integer offset,
                                                 @Query("limit") Integer limit);
}
