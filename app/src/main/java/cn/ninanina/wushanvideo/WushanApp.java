package cn.ninanina.wushanvideo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.ads.MobileAds;

import org.apache.commons.lang3.StringUtils;

import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.util.AppOpenManager;

public class WushanApp extends Application {
    private static WushanApp application;

    private AppOpenManager appOpenManager;

    private SharedPreferences profile;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        Fresco.initialize(this);
        MobileAds.initialize(this, initializationStatus -> {
        });
        appOpenManager = new AppOpenManager(this);
        profile = getSharedPreferences("profile", MODE_PRIVATE);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public static WushanApp getInstance() {
        return application;
    }

    public static SharedPreferences getProfile() {
        return application.profile;
    }

    public static String getAppKey() {
        return getProfile().getString("appKey", "jdfohewk");
    }

    public static boolean loggedIn() {
        return !StringUtils.isEmpty(getProfile().getString("username", ""));
    }
}
