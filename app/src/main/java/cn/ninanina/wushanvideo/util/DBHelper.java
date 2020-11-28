package cn.ninanina.wushanvideo.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;

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
                "url VARCHAR(255), " +
                "coverUrl VARCHAR(255), " +
                "duration VARCHAR(255))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void saveVideo(VideoDetail videoDetail) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("_id", videoDetail.getId());
        values.put("title", videoDetail.getTitle());
        if (!StringUtils.isEmpty(videoDetail.getTitleZh()))
            values.put("titleZh", videoDetail.getTitleZh());
        values.put("name", videoDetail.getTitle().trim() + ".mp4");
        values.put("coverUrl", videoDetail.getCoverUrl());
        values.put("duration", videoDetail.getDuration());
        db.insert(DATABASE_NAME, null, values);
    }

    public VideoDetail getVideo(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("video", new String[]{"*"}, "name = ?", new String[]{name}, null, null, null);
        VideoDetail videoDetail = new VideoDetail();
        while (cursor.moveToNext()) {
            videoDetail.setId(cursor.getLong(cursor.getColumnIndex("_id")));
            videoDetail.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            videoDetail.setTitleZh(cursor.getString(cursor.getColumnIndex("titleZh")));
            videoDetail.setCoverUrl(cursor.getString(cursor.getColumnIndex("coverUrl")));
            videoDetail.setDuration(cursor.getString(cursor.getColumnIndex("duration")));
        }
        cursor.close();
        return videoDetail;
    }

    public boolean downloaded(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("video", new String[]{"*"}, "_id = ?", new String[]{String.valueOf(id)}, null, null, null);
        boolean result = cursor.moveToNext();
        cursor.close();
        return result;
    }
}
