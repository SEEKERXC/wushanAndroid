package cn.ninanina.wushanvideo.ui.video;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.githang.statusbar.StatusBarCompat;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.PlaylistAdapter;
import cn.ninanina.wushanvideo.adapter.PlaylistVideoAdapter;
import cn.ninanina.wushanvideo.adapter.SingleVideoListAdapter;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoClickListener;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoOptionClickListener;
import cn.ninanina.wushanvideo.adapter.listener.PlaylistVideoOptionClickListener;
import cn.ninanina.wushanvideo.adapter.listener.ShowPlaylistClickListener;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.util.DialogManager;
import cn.ninanina.wushanvideo.util.TimeUtil;

public class PlaylistActivity extends AppCompatActivity {
    @BindView(R.id.back)
    FrameLayout back;
    @BindView(R.id.edit)
    TextView edit;
    @BindView(R.id.playlist_cover)
    SimpleDraweeView cover;
    @BindView(R.id.playlist_name)
    TextView nameText;
    @BindView(R.id.playlist_info)
    TextView infoText;
    @BindView(R.id.more_info)
    TextView moreInfo;
    @BindView(R.id.playlist_content)
    RecyclerView content;

    private Playlist playlist;

    public static Handler handler;
    public static final int deleteOne = 1;
    public static final int updateInfo = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        ButterKnife.bind(this);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.white, null), true);
        content.setLayoutManager(new LinearLayoutManager(this));
        initData();
        bindEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler = null;
    }

    private void bindEvent() {
        back.setOnClickListener(v -> PlaylistActivity.this.finish());
        edit.setOnClickListener(v -> DialogManager.getInstance().newPlaylistOptionDialog(PlaylistActivity.this, playlist).show());
    }

    private void initData() {
        Intent intent = getIntent();
        playlist = (Playlist) intent.getSerializableExtra("playlist");

        refreshData();
        PlaylistVideoAdapter adapter = new PlaylistVideoAdapter(DataHolder.getInstance().getPlaylistVideos(playlist.getId()).getVideoDetails(),
                new DefaultVideoClickListener(this), new PlaylistVideoOptionClickListener(this, playlist));
        content.setAdapter(adapter);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == deleteOne) {
                    adapter.deleteOne(msg.arg1);
                    refreshData();
                } else if (msg.what == updateInfo) {
                    refreshData();
                }
                super.handleMessage(msg);
            }
        };
    }

    private void refreshData() {
        cover.setImageURI(playlist.getCover());
        nameText.setText(playlist.getName());
        infoText.setText(playlist.getCount() + "个视频 · " + (playlist.getIsPublic() ? "公开" : "私有"));
        moreInfo.setText(TimeUtil.getDate(playlist.getUpdateTime()) + "更新");
    }

    public RecyclerView getContent() {
        return content;
    }

}