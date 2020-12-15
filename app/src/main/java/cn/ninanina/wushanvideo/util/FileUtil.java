package cn.ninanina.wushanvideo.util;

import android.os.Environment;

import androidx.core.content.ContextCompat;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Locale;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;

public class FileUtil {
    public static File getVideoDir() {
        return ContextCompat.getExternalFilesDirs(WushanApp.getInstance(), Environment.DIRECTORY_MOVIES)[0];
    }

    public static File getCoverDir() {
        return ContextCompat.getExternalFilesDirs(WushanApp.getInstance(), Environment.DIRECTORY_PICTURES)[0];
    }

    public static String getVideoFileName(VideoDetail videoDetail) {
        String fileName;
        if (StringUtils.isEmpty(videoDetail.getTitleZh())) fileName = videoDetail.getTitle();
        else fileName = videoDetail.getTitleZh();
        fileName = fileName.replaceAll("/", "").trim() + ".mp4";
        return fileName;
    }

    public static String getSize(long byteLength) {
        if (byteLength < 1024) return "0KB";
        else if (byteLength < 1024 * 1024) {
            return byteLength / 1024 + "KB";
        } else if (byteLength < 1024 * 1024 * 1024) {
            double MB = (double) byteLength / (1024 * 1024);
            return String.format(Locale.CHINA, "%.1f", MB) + "MB";
        } else {
            double GB = (double) byteLength / (1024 * 1024 * 1024);
            return String.format(Locale.CHINA, "%.2f", GB) + "GB";
        }
    }
}
