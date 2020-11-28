package cn.ninanina.wushanvideo.util;

import android.os.Environment;

import androidx.core.content.ContextCompat;

import java.io.File;

import cn.ninanina.wushanvideo.WushanApp;

public class FileUtil {
    public static File getVideoDir() {
        return ContextCompat.getExternalFilesDirs(WushanApp.getInstance(), Environment.DIRECTORY_MOVIES)[0];
    }

    public static File getCoverDir() {
        return ContextCompat.getExternalFilesDirs(WushanApp.getInstance(), Environment.DIRECTORY_PICTURES)[0];
    }
}
