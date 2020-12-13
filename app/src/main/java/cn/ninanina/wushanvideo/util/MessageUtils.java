package cn.ninanina.wushanvideo.util;


import android.os.Handler;
import android.os.Message;

import cn.ninanina.wushanvideo.ui.me.MeFragment;

public class MessageUtils {
    public static void updatePlaylist() {
        Handler handler = MeFragment.handler;
        if (handler != null) {
            Message message = new Message();
            message.what = MeFragment.updatePlaylist;
            MeFragment.handler.sendMessage(message);
        }
    }
}
