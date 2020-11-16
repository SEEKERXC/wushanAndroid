package cn.ninanina.wushanvideo.ui.me;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.network.CommonPresenter;

public class MeFragment extends Fragment {
    @BindView(R.id.fragment_me_top)
    LinearLayout top;
    @BindView(R.id.welcome)
    TextView welcome;
    @BindView(R.id.login_register_button)
    Button login_register_button;
    @BindView(R.id.me_menu)
    LinearLayout menu;
    @BindView(R.id.menu_collect)
    LinearLayout menuCollect;
    @BindView(R.id.menu_download)
    LinearLayout menuDownload;
    @BindView(R.id.menu_history)
    LinearLayout menuHistory;
    @BindView(R.id.menu_calendar)
    LinearLayout menuCalendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_me, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initMenu();
        login_register_button.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
        });
        checkForUser();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkForUser();
    }

    private void checkForUser() {
        SharedPreferences profile = WushanApp.getProfile();
        if (!StringUtils.isEmpty(profile.getString("username", ""))) {
            String nickname = profile.getString("nickname", "");
            top.removeView(login_register_button);
            welcome.setText("欢迎来到巫山小视频 ~ " + nickname);
            CommonPresenter.getInstance().checkForLogin();
        }
    }

    private void initMenu() {

    }

}
