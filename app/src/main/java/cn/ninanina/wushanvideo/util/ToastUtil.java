package cn.ninanina.wushanvideo.util;

import android.content.Context;
import android.widget.Toast;

import cn.ninanina.wushanvideo.ui.MainActivity;

public class ToastUtil {
    private static Toast toast;

    public synchronized static void show(String text) {
        MainActivity.getInstance().runOnUiThread(() -> {
            if (toast == null) {
                toast = Toast.makeText(MainActivity.getInstance().getApplicationContext(), text, Toast.LENGTH_SHORT);
            } else {
                toast.setText(text);
            }
            toast.show();
        });
    }
}
