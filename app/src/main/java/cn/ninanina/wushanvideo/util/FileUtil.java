package cn.ninanina.wushanvideo.util;

import android.os.Environment;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.Locale;

import cn.ninanina.wushanvideo.WushanApp;

public class FileUtil {
    public static File getVideoDir() {
        return ContextCompat.getExternalFilesDirs(WushanApp.getInstance(), Environment.DIRECTORY_MOVIES)[0];
    }

    public static File getCoverDir() {
        return ContextCompat.getExternalFilesDirs(WushanApp.getInstance(), Environment.DIRECTORY_PICTURES)[0];
    }

    public static String getSize(long byteLength) {
        if (byteLength < 1024) return "0KB";
        else if (byteLength < 1024 * 1024) {
            return byteLength / 1024 + "KB";
        } else {
            double MB = (double) byteLength / (1024 * 1024);
            return String.format(Locale.CHINA, "%.1f", MB) + "MB";
        }
    }
}
