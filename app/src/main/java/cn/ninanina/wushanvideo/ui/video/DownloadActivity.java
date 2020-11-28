package cn.ninanina.wushanvideo.ui.video;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.githang.statusbar.StatusBarCompat;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.DownloadVideoListAdapter;
import cn.ninanina.wushanvideo.adapter.SingleVideoListAdapter;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoClickListener;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoOptionClickListener;
import cn.ninanina.wushanvideo.adapter.listener.OfflineVideoClickListener;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.util.DBHelper;
import cn.ninanina.wushanvideo.util.FileUtil;

public class DownloadActivity extends AppCompatActivity {
    @BindView(R.id.download_list)
    RecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.white, null), true);
        list.setLayoutManager(new LinearLayoutManager(this));
        initVideoList();
    }

    private void initVideoList() {
        DBHelper dbHelper = new DBHelper(MainActivity.getInstance());
        File dir = FileUtil.getVideoDir();
        File[] videos = dir.listFiles();
        List<VideoDetail> videoDetails = new ArrayList<>();
        if (videos != null)
            for (File file : videos) {
                String name = file.getName();
                if (!name.endsWith(".mp4")) continue;
                VideoDetail videoDetail = dbHelper.getVideo(name);
                videoDetail.setSrc(dir.getAbsolutePath() + "/" + name);
                videoDetail.setUpdateTime(file.lastModified());
                if (videoDetail.getId() == null) {
                    videoDetail.setTitle(file.getName().substring(0, file.getName().length() - 4));
                }

                videoDetails.add(videoDetail);
            }
        list.setAdapter(new DownloadVideoListAdapter(new ArrayList<>(videoDetails),
                new OfflineVideoClickListener(this),
                new DefaultVideoOptionClickListener(this)));
    }
}