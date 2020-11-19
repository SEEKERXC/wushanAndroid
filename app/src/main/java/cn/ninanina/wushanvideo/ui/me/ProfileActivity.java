package cn.ninanina.wushanvideo.ui.me;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.githang.statusbar.StatusBarCompat;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;

public class ProfileActivity extends AppCompatActivity {
    @BindView(R.id.profile_username)
    TextView usernameText;
    @BindView(R.id.profile_password)
    TextView passwordText;
    @BindView(R.id.profile_nickname)
    TextView nicknameText;
    @BindView(R.id.profile_gender)
    TextView genderText;
    @BindView(R.id.profile_age)
    TextView ageText;
    @BindView(R.id.profile_orientation)
    TextView orientationText;
    @BindView(R.id.profile_register)
    TextView registerText;
    @BindView(R.id.profile_login)
    TextView loginText;
    @BindView(R.id.profile_password_layout)
    ConstraintLayout passwordLayout;
    @BindView(R.id.profile_nickname_layout)
    ConstraintLayout nicknameLayout;
    @BindView(R.id.profile_gender_layout)
    ConstraintLayout genderLayout;
    @BindView(R.id.profile_age_layout)
    ConstraintLayout ageLayout;
    @BindView(R.id.profile_orientation_layout)
    ConstraintLayout orientationLayout;
    @BindView(R.id.profile_swipe)
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.transparent, null), true);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        SharedPreferences profile = WushanApp.getProfile();
        usernameText.setText(profile.getString("username", ""));
        int passLen = Objects.requireNonNull(profile.getString("password", "")).length();
        StringBuilder pass = new StringBuilder();
        for (int i = 0; i < passLen; i++) pass.append('*');
        passwordText.setText(pass.toString());
        nicknameText.setText(profile.getString("nickname", ""));
        Calendar registerC = Calendar.getInstance();
        registerC.setTimeInMillis(profile.getLong("registerTime", 0));
        String register = registerC.get(Calendar.YEAR) + "年" + (registerC.get(Calendar.MONTH) + 1) + "月" + registerC.get(Calendar.DAY_OF_MONTH) + "日 "
                + registerC.get(Calendar.HOUR) + ":" + registerC.get(Calendar.MINUTE);
        registerText.setText(register);
        Calendar loginC = Calendar.getInstance();
        loginC.setTimeInMillis(profile.getLong("lastLoginTime", 0));
        String login = loginC.get(Calendar.YEAR) + "年" + loginC.get(Calendar.MONTH) + 1 + "月" + loginC.get(Calendar.DAY_OF_MONTH) + "日 "
                + loginC.get(Calendar.HOUR) + ":" + loginC.get(Calendar.MINUTE);
        loginText.setText(login);
        genderText.setText(profile.getString("gender", ""));
    }
}