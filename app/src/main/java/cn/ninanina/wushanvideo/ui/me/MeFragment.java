package cn.ninanina.wushanvideo.ui.me;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.common.util.CollectionUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.PlaylistAdapter;
import cn.ninanina.wushanvideo.adapter.listener.PlaylistLongClickListener;
import cn.ninanina.wushanvideo.adapter.listener.ShowPlaylistClickListener;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.ui.home.HistoryActivity;
import cn.ninanina.wushanvideo.ui.home.LikeActivity;
import cn.ninanina.wushanvideo.ui.home.WatchLaterActivity;
import cn.ninanina.wushanvideo.ui.video.DownloadActivity;
import cn.ninanina.wushanvideo.util.DialogManager;

public class MeFragment extends Fragment {
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    @BindView(R.id.fragment_me_top)
    LinearLayout top;
    @BindView(R.id.welcome)
    TextView welcome;
    @BindView(R.id.login_register_button)
    Button login_register_button;
    @BindView(R.id.me_menu)
    LinearLayout menu;
    @BindView(R.id.menu_profile)
    LinearLayout menuProfile;
    @BindView(R.id.menu_collect)
    LinearLayout menuCollect;
    @BindView(R.id.collect_text)
    TextView collectText;
    @BindView(R.id.collect_icon)
    ImageView collectIcon;
    @BindView(R.id.collect_new_dir)
    ConstraintLayout newCollectDir;
    @BindView(R.id.collect_list)
    RecyclerView playlist;
    @BindView(R.id.menu_download)
    LinearLayout menuDownload;
    @BindView(R.id.menu_history)
    LinearLayout menuHistory;
    @BindView(R.id.menu_later)
    LinearLayout watchLater;
    @BindView(R.id.menu_like)
    LinearLayout menuLike;
    //    @BindView(R.id.menu_calendar)
//    LinearLayout menuCalendar;
    @BindView(R.id.menu_setting)
    LinearLayout menuSetting;
    @BindView(R.id.menu_info)
    LinearLayout menuInfo;

    private boolean showPlaylists = true;

    public static Handler handler;
    public static final int updatePlaylist = 0;
    public static final int login = 1;
    public static final int logout = 2;

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
        playlist.setLayoutManager(new LinearLayoutManager(getContext()));
        login_register_button.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
        });
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case updatePlaylist:
                        refreshPlaylist();
                        break;
                    case login:
                        break;
                    case logout:
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        checkForUser();
        refreshPlaylist();
    }

    private void checkForUser() {
        SharedPreferences profile = WushanApp.getProfile();
        if (!StringUtils.isEmpty(profile.getString("username", ""))) {
            String nickname = profile.getString("nickname", "");
            top.removeView(login_register_button);
            welcome.setText("欢迎来到巫山小视频 ~ " + nickname);
        } else {
            if (top.indexOfChild(login_register_button) < 0) {
                top.addView(login_register_button, 1);
            }
            welcome.setText(R.string.welcome);
        }
    }

    private void initMenu() {
        swipe.setOnRefreshListener(() -> {
            refreshPlaylist();
            swipe.setRefreshing(false);
        });
        menuProfile.setOnClickListener(v -> {
            if (WushanApp.loggedIn()) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            }

        });
        menuSetting.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        });
        menuCollect.setOnClickListener(v -> {
            showPlaylists = !showPlaylists;
            refreshPlaylist();
        });
        newCollectDir.setOnClickListener(v -> {
            if (WushanApp.loggedIn())
                DialogManager.getInstance().newCreatePlaylistDialog(MeFragment.this).show();
            else DialogManager.getInstance().newLoginDialog(getActivity()).show();
        });
        menuDownload.setOnClickListener(v -> {
            if (WushanApp.loggedIn()) {
                Intent intent = new Intent(getContext(), DownloadActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
        menuHistory.setOnClickListener(v -> {
            if (!WushanApp.loggedIn()) {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getContext(), HistoryActivity.class);
                startActivity(intent);
            }
        });
        watchLater.setOnClickListener(v -> {
            if (!WushanApp.loggedIn()) {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getContext(), WatchLaterActivity.class);
                startActivity(intent);
            }
        });
        menuLike.setOnClickListener(v -> {
            if (!WushanApp.loggedIn()) {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getContext(), LikeActivity.class);
                startActivity(intent);
            }
        });
    }

    public void refreshPlaylist() {
        if (playlist == null) return;
        if (WushanApp.loggedIn()) {
            if (!CollectionUtils.isEmpty(DataHolder.getInstance().getPlaylists())) {
                if (showPlaylists) {
                    PlaylistAdapter adapter = new PlaylistAdapter(DataHolder.getInstance().getPlaylists(), new ShowPlaylistClickListener(getContext()));
                    adapter.setLongClickListener(new PlaylistLongClickListener(getActivity()));
                    playlist.setAdapter(adapter);
                    collectIcon.setImageResource(R.drawable.down);
                } else {
                    playlist.setAdapter(new PlaylistAdapter(new ArrayList<>(), new ShowPlaylistClickListener(getContext())));
                    collectIcon.setImageResource(R.drawable.up);
                }
            } else {
                PlaylistAdapter adapter = (PlaylistAdapter) playlist.getAdapter();
                if (adapter != null) adapter.clear();
            }
        } else {
            PlaylistAdapter adapter = (PlaylistAdapter) playlist.getAdapter();
            if (adapter != null) adapter.clear();
        }
    }

    public RecyclerView getPlaylist() {
        return playlist;
    }
}
