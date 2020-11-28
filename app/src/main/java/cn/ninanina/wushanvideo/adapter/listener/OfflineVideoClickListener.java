package cn.ninanina.wushanvideo.adapter.listener;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;

import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;

public class OfflineVideoClickListener implements VideoClickListener {
    public OfflineVideoClickListener(Context context) {
        this.context = context;
    }

    private Context context;

    @Override
    public void onVideoClicked(VideoDetail videoDetail) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, "cn.ninanina.wushanvideo.fileProvider", new File(videoDetail.getSrc()));
            intent.setDataAndType(contentUri, "video/*");
        } else {
            intent.setDataAndType(Uri.fromFile(new File(videoDetail.getSrc())), "video/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
