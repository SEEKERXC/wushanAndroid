package cn.ninanina.wushanvideo.util;

import android.content.Context;

import java.util.Calendar;

public class TimeUtil {
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

    public static String getDate(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日 ";
    }
}
