package cn.ninanina.wushanvideo.model.api;

import org.apache.commons.lang3.ObjectUtils;

import cn.ninanina.wushanvideo.model.bean.Result;
import cn.ninanina.wushanvideo.model.bean.common.Feedback;
import cn.ninanina.wushanvideo.model.bean.common.Pair;
import cn.ninanina.wushanvideo.model.bean.common.User;
import cn.ninanina.wushanvideo.model.bean.common.VersionInfo;
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
    Observable<Result<Pair<String, User>>> register(@Query("appKey") String appKey,
                                                    @Query("username") String username,
                                                    @Query("password") String password,
                                                    @Query("nickname") String nickname,
                                                    @Query("gender") String gender);

    @GET("user/loggedIn")
    Observable<Result<ObjectUtils.Null>> checkLogin(@Query("appKey") String appKey,
                                                    @Query("token") String token);

    @POST("user/login")
    Observable<Result<Pair<String, User>>> login(@Query("appKey") String appKey,
                                                 @Query("username") String username,
                                                 @Query("password") String password);

    @POST("user/logout")
    Observable<Result<ObjectUtils.Null>> logout(@Query("appKey") String appKey,
                                                @Query("token") String token);

    @POST("user/update")
    Observable<Result<User>> updateUser(@Query("appKey") String appKey,
                                        @Query("token") String token,
                                        @Query("gender") String gender,
                                        @Query("password") String password,
                                        @Query("nickname") String nickname,
                                        @Query("age") Integer age,
                                        @Query("straight") Boolean straight);

    @GET("common/version")
    Observable<Result<VersionInfo>> getVersion();

    @POST("common/feedback")
    Observable<Result<Feedback>> sendFeedback(@Query("appKey") String appKey,
                                              @Query("token") String token,
                                              @Query("content") String content);

    @GET("common/contact")
    Observable<Result<String>> getContact(@Query("appKey") String appKey);

    @GET("common/protocol")
    Observable<Result<String>> getProtocol(@Query("appKey") String appKey);

}
