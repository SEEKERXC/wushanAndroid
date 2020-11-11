package cn.ninanina.wushanvideo.model.api;

import java.util.List;

import cn.ninanina.wushanvideo.model.bean.Result;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface VideoService {
    @GET("video/recommend")
    Observable<Result<List<VideoDetail>>> getRecommend(@Query("appKey") String appKey,
                                                       @Query("limit") int limit);

    @GET("video/detail")
    Observable<Result<VideoDetail>> getVideoDetail(@Query("id") Long id);
}
