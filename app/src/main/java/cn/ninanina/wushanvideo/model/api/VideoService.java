package cn.ninanina.wushanvideo.model.api;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import cn.ninanina.wushanvideo.model.bean.Result;
import cn.ninanina.wushanvideo.model.bean.video.Comment;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.model.bean.video.VideoDir;
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
                                                       @Query("limit") int limit);

    //获取视频链接
    @GET("video/detail")
    Observable<Result<VideoDetail>> getVideoDetail(@Query("appKey") String appKey,
                                                   @Query("id") long id);

    //获取相关视频
    @GET("video/related")
    Observable<Result<List<VideoDetail>>> getRelatedVideos(@Query("appKey") String appKey,
                                                           @Query("id") long id);

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
                                          @Query("parentId") Long parentId);

    //创建收藏夹
    @POST("video/collect/create")
    Observable<Result<VideoDir>> createVideoDir(@Query("appKey") String appKey,
                                                @Query("name") String name);

    //删除收藏夹
    @POST("video/collect/delete")
    Observable<Result<ObjectUtils.Null>> deleteVideoDir(@Query("appKey") String appKey,
                                                        @Query("dirId") Long dirId);

    //重命名收藏夹
    @POST("video/collect/rename")
    Observable<Result<VideoDir>> renameVideoDir(@Query("appKey") String appKey,
                                                @Query("dirId") Long dirId,
                                                @Query("name") String name);

    //获取收藏夹列表
    @GET("video/collect")
    Observable<Result<List<VideoDir>>> collectList(@Query("appKey") String appKey);

    //收藏视频
    @POST("video/collect")
    Observable<Result<ObjectUtils.Null>> collectVideo(@Query("appKey") String appKey,
                                                      @Query("videoId") Long videoId,
                                                      @Query("dirId") Long dirId);

    //取消收藏视频
    @POST("video/collect/cancel")
    Observable<Result<ObjectUtils.Null>> cancelCollect(@Query("appKey") String appKey,
                                                       @Query("videoId") Long videoId,
                                                       @Query("dirId") Long dirId);

    //搜索视频
    @GET("video/search")
    Observable<Result<List<VideoDetail>>> search(@Query("appKey") String appKey,
                                                 @Query("query") String query,
                                                 @Query("offset") Integer offset,
                                                 @Query("limit") Integer limit);
}
