package cn.ninanina.wushanvideo.util;

import android.content.Context;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;

public class TimeUtil {
    //yyyy年MM月dd日 HH:mm
    public static String getFullTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        String result = calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日 ";
        if (calendar.get(Calendar.HOUR_OF_DAY) <= 9)
            result += ("0" + calendar.get(Calendar.HOUR_OF_DAY) + ":");
        else result += (calendar.get(Calendar.HOUR_OF_DAY) + ":");
        if (calendar.get(Calendar.MINUTE) <= 9)
            result += ("0" + calendar.get(Calendar.MINUTE));
        else result += calendar.get(Calendar.MINUTE);
        return result;
    }

    //MM-dd
    public static String getSimpleDate(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static String getDate(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日 ";
    }

    public static String getTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR_OF_DAY))
                + ":" + ((calendar.get(Calendar.MINUTE) < 10 ? "0" + calendar.get(Calendar.MINUTE) : calendar.get(Calendar.MINUTE)));
    }

    /**
     * 根据一个毫秒数获取对应的时间段，用HH:mm:ss表示
     */
    public static String getDuration(long millis) {
        if (millis < 0) return "";
        long seconds = millis / 1000;
        if (seconds < 60) {
            return "00:" + (seconds < 10 ? "0" + seconds : seconds);
        } else if (seconds < 3600) {
            long minute = seconds / 60;
            long second = seconds % 60;
            return (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second);
        } else {
            long hour = seconds / 3600;
            long minute = (seconds % 3600) / 60;
            long second = seconds % 3600 % 60;
            return (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second);
        }
    }

    /**
     * 将秒时间段转换成hh小时mm分钟的形式
     */
    public static String getDurationZh(int seconds) {
        int minutes = seconds / 60;
        if (minutes < 60) return minutes + "分钟";
        else {
            int hours = minutes / 60;
            minutes = minutes - hours * 60;
            if (minutes > 0) return hours + "小时" + minutes + "分钟";
            else return hours + "小时";
        }
    }

    public static boolean isToday(long millis) {
        return getSimpleDate(System.currentTimeMillis()).equals(getSimpleDate(millis));
    }
}
