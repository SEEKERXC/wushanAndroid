package cn.ninanina.wushanvideo.ui.me;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.githang.statusbar.StatusBarCompat;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.common.Constants;
import cn.ninanina.wushanvideo.model.bean.common.VersionInfo;
import cn.ninanina.wushanvideo.network.CommonPresenter;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.util.DialogManager;
import cn.ninanina.wushanvideo.util.ToastUtil;

public class AboutActivity extends AppCompatActivity {
    @BindView(R.id.back)
    FrameLayout back;
    @BindView(R.id.update)
    ConstraintLayout update;
    @BindView(R.id.new_version)
    TextView newVersion;
    @BindView(R.id.version)
    TextView version;
    @BindView(R.id.feedback)
    ConstraintLayout feedback;
    @BindView(R.id.about_app)
    ConstraintLayout aboutApp;
    @BindView(R.id.contact)
    ConstraintLayout contact;
    @BindView(R.id.email)
    TextView email;
    @BindView(R.id.share)
    ConstraintLayout share;
    @BindView(R.id.protocol)
    ConstraintLayout protocol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.white, null), true);
        ButterKnife.bind(this);
        version.setText(Constants.VERSION);
        bindEvents();
        checkForVersion();
    }

    private void bindEvents() {
        back.setOnClickListener(v -> AboutActivity.this.finish());
        feedback.setOnClickListener(v -> {
            if (!WushanApp.loggedIn()) {
                DialogManager.getInstance().newLoginDialog(AboutActivity.this).show();
            } else {
                DialogManager.getInstance().newFeedbackDialog(AboutActivity.this).show();
            }
        });
        CommonPresenter.getInstance().getContact(email);
        contact.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) MainActivity.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText(null, email.getText());
            clipboard.setPrimaryClip(clipData);
            ToastUtil.show("已复制到剪切板");
        });
        aboutApp.setOnClickListener(v -> {
            Intent intent = new Intent(AboutActivity.this, AboutAppActivity.class);
            startActivity(intent);
        });
        share.setOnClickListener(v -> {
            VersionInfo versionInfo = DataHolder.getInstance().getNewVersion();
            if (versionInfo == null) return;
            ClipboardManager clipboard = (ClipboardManager) MainActivity.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText(null, versionInfo.getAppUrl());
            clipboard.setPrimaryClip(clipData);
            ToastUtil.show("已复制下载链接到剪切板");
        });
        protocol.setOnClickListener(v -> {
            Intent intent = new Intent(AboutActivity.this, ProtocolActivity.class);
            startActivity(intent);
        });
    }

    private void checkForVersion() {
        SharedPreferences constants = WushanApp.getConstants();
        String nowVersion = constants.getString("version", Constants.VERSION);
        VersionInfo versionInfo = DataHolder.getInstance().getNewVersion();
        if (versionInfo != null && !versionInfo.getVersionCode().equals(nowVersion)) {
            newVersion.setVisibility(View.VISIBLE);
            update.setOnClickListener(v -> {
                DialogManager.getInstance().newUpdateDialog(AboutActivity.this, versionInfo).show();
            });
        }
    }
}