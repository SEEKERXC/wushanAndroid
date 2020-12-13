package cn.ninanina.wushanvideo.util;

import org.apache.commons.lang3.StringUtils;

public class CommonUtils {

    public static boolean isSrcValid(String src) {
        if (StringUtils.isEmpty(src)) return false;
        if (src.contains(FileUtil.getVideoDir().getAbsolutePath())) return true; //本地视频
        long currentSeconds = System.currentTimeMillis() / 1000;
        long urlSeconds = Long.parseLong(src.substring(src.indexOf("?e=") + 3, src.indexOf("&h="))) - 1800;
        return currentSeconds < urlSeconds;
    }

    public static String getViewsString(int viewed) {
        StringBuilder strViewed = new StringBuilder();
        if (viewed >= 10000) {
            int wan = viewed / 10000;
            int qian = viewed % 10000 / 1000;
            strViewed.append(wan);
            if (qian != 0) strViewed.append('.').append(qian);
            strViewed.append("万");
        } else strViewed.append(viewed);
        return strViewed.toString();
    }

    public static String getDurationString(String duration) {
        return duration.replace(" ", "\0").replace("min", "分钟").replace("h", "小时").replace("sec", "秒");
    }

    //将h/min/sec的时间格式转换成秒数
    public static int getDurationSeconds(String duration) {
        int seconds = 0;
        if (duration.contains("h")) {
            seconds += Integer.parseInt(duration.substring(0, duration.indexOf('h')).trim()) * 3600;
        }
        if (duration.contains("min")) {
            int start = 0;
            if (duration.contains("h")) start = duration.indexOf("h") + 1;
            seconds += Integer.parseInt(duration.substring(start, duration.indexOf("min")).trim()) * 60;
        }
        if (duration.contains("sec")) {
            int start = 0;
            if (duration.contains("min")) start = duration.indexOf("min") + 1;
            seconds += Integer.parseInt(duration.substring(start, duration.indexOf("sec")).trim());
        }
        return seconds;
    }
}
