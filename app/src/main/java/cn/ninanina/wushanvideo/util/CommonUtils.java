package cn.ninanina.wushanvideo.util;

import org.apache.commons.lang3.StringUtils;

public class CommonUtils {

    public static boolean isSrcValid(String src) {
        if (StringUtils.isEmpty(src)) return false;
        long currentSeconds = System.currentTimeMillis() / 1000;
        long urlSeconds = Long.parseLong(src.substring(src.indexOf("?e=") + 3, src.indexOf("&h="))) - 1800;
        return currentSeconds < urlSeconds;
    }
}
