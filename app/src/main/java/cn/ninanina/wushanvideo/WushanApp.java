package cn.ninanina.wushanvideo;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class WushanApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
