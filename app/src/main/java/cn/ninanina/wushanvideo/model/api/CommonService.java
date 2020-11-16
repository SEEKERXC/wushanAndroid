package cn.ninanina.wushanvideo.model.api;

import org.apache.commons.lang3.ObjectUtils;

import cn.ninanina.wushanvideo.model.bean.Result;
import cn.ninanina.wushanvideo.model.bean.common.User;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 所有通用API和用户API都在此
 */
public interface CommonService {
    @POST("common/genAppkey")
    Observable<Result<String>> genAppkey(@Query("secret") String secret);

    @GET("user/exist")
    Observable<Result<String>> userExists(@Query("appKey") String appKey,
                                          @Query("username") String username);

    @POST("user/register")
    Observable<Result<User>> register(@Query("appKey") String appKey,
                                      @Query("username") String username,
                                      @Query("password") String password,
                                      @Query("nickname") String nickname,
                                      @Query("gender") String gender);

    @GET("user/loggedIn")
    Observable<Result<User>> checkLogin(@Query("appKey") String appKey);

    @POST("user/login")
    Observable<Result<User>> login(@Query("appKey") String appKey,
                                   @Query("username") String username,
                                   @Query("password") String password);
}
