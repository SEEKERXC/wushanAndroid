package cn.ninanina.wushanvideo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.internal.$Gson$Preconditions;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.common.VersionInfo;
import cn.ninanina.wushanvideo.network.AdManager;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.util.AppOpenManager;
import cn.ninanina.wushanvideo.util.DBHelper;

public class WushanApp extends Application {
    private static WushanApp application;
    private DBHelper dbHelper;

    private AppOpenManager appOpenManager;

    private SharedPreferences profile;
    private SharedPreferences constants;

    //标注加载完视频的收藏夹数量
    public int playlistLoadingFinished = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        Fresco.initialize(this);
        MobileAds.initialize(this, initializationStatus -> {
        });
        appOpenManager = new AppOpenManager(this);
        profile = getSharedPreferences("profile", MODE_PRIVATE);
        constants = getSharedPreferences("constants", MODE_PRIVATE);
        dbHelper = new DBHelper(this);
        AdManager.getInstance().loadAds(5);
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

    public static SharedPreferences getConstants() {
        return application.constants;
    }

    public static String getAppKey() {
        return getProfile().getString("appKey", "d19dd096b926be60ef069493f277711c7a979f2e073b364d8a3aead2d85aa543");
    }

    public static boolean loggedIn() {
        return !StringUtils.isEmpty(getProfile().getString("username", ""));
    }

    public List<Activity> getActivities() {
        return activities;
    }

    private List<Activity> activities = new ArrayList<>();

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        dbHelper.close();

        for (Activity activity : activities) {
            activity.finish();
        }

        System.exit(0);
    }

    //在登录之后初始化数据
    public void initData() {
        VideoPresenter.getInstance().loadPlaylists();
        VideoPresenter.getInstance().loadLikedAndDisliked();
        VideoPresenter.getInstance().loadAllHistory();
    }

    public DBHelper getDbHelper() {
        return dbHelper;
    }
}
