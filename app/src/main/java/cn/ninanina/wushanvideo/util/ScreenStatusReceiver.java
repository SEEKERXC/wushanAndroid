package cn.ninanina.wushanvideo.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenStatusReceiver extends BroadcastReceiver {
    String SCREEN_ON = "android.intent.action.SCREEN_ON";
    String SCREEN_OFF = "android.intent.action.SCREEN_OFF";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SCREEN_ON.equals(intent.getAction())) {
            PlayTimeManager.continueTiming();
        } else if (SCREEN_OFF.equals(intent.getAction())) {
            PlayTimeManager.stopTiming();
        }
    }
}
