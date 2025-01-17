package cn.ninanina.wushanvideo.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.video.DownloadedFragment;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "video";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS video(_id INTEGER PRIMARY KEY, " +
                "title VARCHAR(255), " +
                "titleZh VARCHAR(255), " +
                "name VARCHAR(255), " +
                "coverUrl VARCHAR(255), " +
                "duration VARCHAR(255))");
        db.execSQL("CREATE TABLE IF NOT EXISTS watch(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "videoId INTEGER, " +
                "day VARCHAR(255), " +
                "time INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("CREATE TABLE IF NOT EXISTS video(_id INTEGER PRIMARY KEY, " +
                "title VARCHAR(255), " +
                "titleZh VARCHAR(255), " +
                "name VARCHAR(255), " +
                "coverUrl VARCHAR(255), " +
                "duration VARCHAR(255))");
        db.execSQL("CREATE TABLE IF NOT EXISTS watch(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "videoId INTEGER, " +
                "day VARCHAR(255), " +
                "time INTEGER)");
    }

    public void saveVideo(VideoDetail videoDetail) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("_id", videoDetail.getId());
        values.put("title", videoDetail.getTitle());
        if (!StringUtils.isEmpty(videoDetail.getTitleZh()))
            values.put("titleZh", videoDetail.getTitleZh());
        values.put("name", FileUtil.getVideoFileName(videoDetail));
        values.put("coverUrl", videoDetail.getCoverUrl());
        values.put("duration", videoDetail.getDuration());
        db.insert(DATABASE_NAME, null, values);
    }

    public VideoDetail getVideo(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("video", new String[]{"*"}, "name = ?", new String[]{name}, null, null, null);
        VideoDetail videoDetail = null;
        while (cursor.moveToNext()) {
            videoDetail = new VideoDetail();
            videoDetail.setId(cursor.getLong(cursor.getColumnIndex("_id")));
            videoDetail.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            videoDetail.setTitleZh(cursor.getString(cursor.getColumnIndex("titleZh")));
            videoDetail.setCoverUrl(cursor.getString(cursor.getColumnIndex("coverUrl")));
            videoDetail.setDuration(cursor.getString(cursor.getColumnIndex("duration")));
        }
        cursor.close();
        return videoDetail;
    }

    public boolean deleteVideo(VideoDetail videoDetail) {
        String name = getNameById(videoDetail.getId());
        File file = new File(FileUtil.getVideoDir() + "/" + name);
        boolean result = false;
        if (file.exists()) {
            result = file.delete();
        }
        SQLiteDatabase db = getWritableDatabase();
        int i = db.delete("video", "name = ?", new String[]{name});
        Handler handler = DownloadedFragment.handler;
        if (i > 0 && result && handler != null) {
            Message message = new Message();
            message.what = DownloadedFragment.deleteOne;
            message.arg1 = Math.toIntExact(videoDetail.getId());
            handler.sendMessage(message);
        }
        return i > 0 && result;
    }

    public boolean renameVideo(VideoDetail videoDetail, File renameTo) {
        File file = new File(FileUtil.getVideoDir().getAbsolutePath() + "/" + getNameById(videoDetail.getId()));
        if (!file.exists()) return false;
        boolean result = file.renameTo(renameTo);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", renameTo.getName());
        int i = db.update("video", values, "_id = ?", new String[]{String.valueOf(videoDetail.getId())});
        Handler handler = DownloadedFragment.handler;
        if (i > 0 && result && handler != null) {
            videoDetail.setSrc(renameTo.getAbsolutePath());
            Message message = new Message();
            message.what = DownloadedFragment.updateOne;
            message.arg1 = Math.toIntExact(videoDetail.getId());
            handler.sendMessage(message);
        }
        return i > 0 && result;
    }

    public String getNameById(long videoId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("video", new String[]{"*"}, "_id = ?", new String[]{String.valueOf(videoId)}, null, null, null);
        String name = "";
        while (cursor.moveToNext()) {
            name = cursor.getString(cursor.getColumnIndex("name"));
        }
        return name;
    }

    public int getCount() {
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select count(1) from video", null);
        while (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * 判断video是否已下载
     *
     * @param id videoId
     * @return 已下载true，未下载false
     */
    public boolean downloaded(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("video", new String[]{"*"}, "_id = ?", new String[]{String.valueOf(id)}, null, null, null);
        boolean result = cursor.moveToNext();
        cursor.close();
        return result;
    }

    /**
     * 增量记录播放时长
     *
     * @param videoId  视频id
     * @param duration 增加的时长，以秒为单位
     */
    public void recordWatch(long videoId, int duration) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        String today = TimeUtil.getDate(System.currentTimeMillis());
        Cursor cursor = db.query("watch", new String[]{"*"}, "videoId = ? and day = ?", new String[]{String.valueOf(videoId), today}, null, null, null);
        if (cursor.moveToNext()) {
            int d = cursor.getInt(cursor.getColumnIndex("time"));
            values.put("time", duration + d);
            db.update("watch", values, "videoId = ? and day = ?", new String[]{String.valueOf(videoId), today});
        } else {
            values.put("videoId", videoId);
            values.put("time", duration);
            values.put("day", today);
            db.insert("watch", null, values);
        }
        cursor.close();
    }

    /**
     * 获取视频的总播放时长，按秒计数
     */
    public int getWatch(long videoId) {
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("watch", new String[]{"*"}, "videoId = ?", new String[]{String.valueOf(videoId)}, null, null, null);
        while (cursor.moveToNext()) {
            count += cursor.getInt(cursor.getColumnIndex("time"));
        }
        cursor.close();
        return count;
    }

    /**
     * 获取某天的总播放时长，按秒计数
     */
    public int getWatchOfDay(String day) {
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("watch", new String[]{"*"}, "day = ?", new String[]{day}, null, null, null);
        while (cursor.moveToNext()) {
            count += cursor.getInt(cursor.getColumnIndex("time"));
        }
        cursor.close();
        return count;
    }
}
