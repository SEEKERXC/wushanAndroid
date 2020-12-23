package cn.ninanina.wushanvideo.ui.me;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.githang.statusbar.StatusBarCompat;

import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("设置");
        }
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.white, null), true);
        WushanApp.getInstance().addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WushanApp.getInstance().removeActivity(this);
    }

}