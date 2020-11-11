package cn.ninanina.wushanvideo.network;

import cn.ninanina.wushanvideo.model.api.VideoService;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class BasePresenter {
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://45.76.206.232/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();

    private VideoService videoService = getRetrofit().create(VideoService.class);

    protected Retrofit getRetrofit(){
        return retrofit;
    }

    protected VideoService getVideoService(){
        return videoService;
    }
}
