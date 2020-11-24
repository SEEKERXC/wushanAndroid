package cn.ninanina.wushanvideo.ui.video;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.githang.statusbar.StatusBarCompat;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.ui.MainActivity;

public class PlaylistActivity extends AppCompatActivity {
    @BindView(R.id.playlist_back)
    ImageView back;
    @BindView(R.id.playlist_cover)
    SimpleDraweeView cover;
    @BindView(R.id.playlist_name)
    TextView nameText;
    @BindView(R.id.playlist_info)
    TextView infoText;
    @BindView(R.id.playlist_content)
    RecyclerView content;

    long id;
    String name;
    String coverUrl;
    int count;
    boolean isPublic;
    long updateTime;
    long createTime;
    long userId;
    String userPhoto;
    String username;

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


    private void bindEvent() {
        back.setOnClickListener(v -> PlaylistActivity.this.finish());
    }

    private void initData() {
        Intent intent = getIntent();
        id = intent.getLongExtra("id", 0);
        name = intent.getStringExtra("name");
        coverUrl = intent.getStringExtra("cover");
        count = intent.getIntExtra("count", 0);
        isPublic = intent.getBooleanExtra("public", true);
        updateTime = intent.getLongExtra("update", 0);
        createTime = intent.getLongExtra("create", 0);
        userId = intent.getLongExtra("userId", 0);
        userPhoto = intent.getStringExtra("userPhoto");
        username = intent.getStringExtra("username");

        cover.setImageURI(coverUrl);
        nameText.setText(name);
        infoText.setText(count + "个视频 · " + (isPublic ? "公开" : "私有"));

        VideoPresenter.getInstance().getVideosForPlaylist(this, id);
    }

    public RecyclerView getContent() {
        return content;
    }

}