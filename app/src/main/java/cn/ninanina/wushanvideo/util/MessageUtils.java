package cn.ninanina.wushanvideo.util;


import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.ui.instant.InstantFragment;
import cn.ninanina.wushanvideo.ui.me.MeFragment;
import cn.ninanina.wushanvideo.ui.video.DetailFragment;
import cn.ninanina.wushanvideo.ui.video.PlaylistActivity;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;

public class MessageUtils {
    public static void updatePlaylist() {
        Handler handler = MeFragment.handler;
        if (handler != null) {
            Message message = new Message();
            message.what = MeFragment.updatePlaylist;
            handler.sendMessage(message);
        }
        Handler handler1 = PlaylistActivity.handler;
        if (handler1 != null) {
            Message message = new Message();
            message.what = PlaylistActivity.updateInfo;
            handler1.sendMessage(message);
        }
    }

    public static void deletePlaylist() {
        Handler handler1 = PlaylistActivity.handler;
        if (handler1 != null) {
            Message message = new Message();
            message.what = PlaylistActivity.deleteThis;
            handler1.sendMessage(message);
        }
    }

    public static void refreshVideoData(VideoDetail videoDetail) {
        if (!MainActivity.getInstance().videoActivityStack.empty()) {
            VideoDetailActivity activity = MainActivity.getInstance().videoActivityStack.peek();
            DetailFragment detailFragment = (DetailFragment) activity.fragments.get(0);
            Handler handler = detailFragment.handler;
            if (handler != null) {
                Message message = new Message();
                message.what = DetailFragment.refreshData;
                handler.sendMessage(message);
            }
        }

        InstantFragment instantFragment = (InstantFragment) MainActivity.getInstance().fragments[2];
        Handler handler1 = instantFragment.handler;
        if (handler1 != null) {
            Message message = new Message();
            message.what = InstantFragment.updateData;
            message.obj = videoDetail;
            handler1.sendMessage(message);
        }
    }

    public static void dislikeVideo(VideoDetail videoDetail) {
        // TODO: 2020/12/18 0018 发送不喜欢视频的消息，对各个activity进行更新
    }
}
