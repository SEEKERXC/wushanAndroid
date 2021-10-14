package cn.ninanina.wushanvideo.network;

import java.net.Socket;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.api.CommonService;
import cn.ninanina.wushanvideo.model.api.VideoService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class BasePresenter {
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://wushantv.online/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();

    private VideoService videoService = getRetrofit().create(VideoService.class);

    private CommonService commonService = getRetrofit().create(CommonService.class);

    private Retrofit getRetrofit() {
        return retrofit;
    }

    protected VideoService getVideoService() {
        return videoService;
    }

    protected CommonService getCommonService() {
        return commonService;
    }

    protected String getAppKey() {
        return WushanApp.getAppKey();
    }

    protected String getToken() {
        return WushanApp.getProfile().getString("token", "");
    }
}
