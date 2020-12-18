package cn.ninanina.wushanvideo.util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.network.VideoPresenter;

/**
 * 管理播放时长
 */
public class PlayTimeManager {
    private static final PlayTimeManager instance = new PlayTimeManager();
    private DBHelper dbHelper;

    private PlayTimeManager() {
        dbHelper = WushanApp.getInstance().getDbHelper();
    }

    //计时存到本地，每秒一次
    private ScheduledExecutorService timingTask;
    //计时存入服务器，每3秒一次
    private ScheduledExecutorService pendingTask;
    private long videoId;

    //开始计时，并且不断向服务器更新
    public static void startTiming(long videoId) {
        instance.videoId = videoId;
        instance.timingTask = new ScheduledThreadPoolExecutor(1);
        instance.pendingTask = new ScheduledThreadPoolExecutor(1);
        instance.timingTask.scheduleAtFixedRate(() ->
                instance.dbHelper.recordWatch(videoId, 1), 1, 1, TimeUnit.SECONDS);
        instance.pendingTask.scheduleAtFixedRate(() ->
                VideoPresenter.getInstance().recordWatch(videoId, 3), 3, 3, TimeUnit.SECONDS);
    }

    public static void continueTiming() {
        if (instance.videoId == 0) return;
        instance.timingTask = new ScheduledThreadPoolExecutor(1);
        instance.pendingTask = new ScheduledThreadPoolExecutor(1);
        instance.timingTask.scheduleAtFixedRate(() ->
                instance.dbHelper.recordWatch(instance.videoId, 1), 1, 1, TimeUnit.SECONDS);
        instance.pendingTask.scheduleAtFixedRate(() ->
                VideoPresenter.getInstance().recordWatch(instance.videoId, 3), 3, 3, TimeUnit.SECONDS);
    }

    //停止计时，停止向服务器更新
    public static void stopTiming() {
        if (instance.timingTask == null || instance.pendingTask == null) return;
        instance.timingTask.shutdown();
        instance.pendingTask.shutdown();
    }

    //获取今天看视频的总时长（秒）
    public static int getTodayWatchTime() {
        String today = TimeUtil.getDate(System.currentTimeMillis());
        return instance.dbHelper.getWatchOfDay(today);
    }
}
