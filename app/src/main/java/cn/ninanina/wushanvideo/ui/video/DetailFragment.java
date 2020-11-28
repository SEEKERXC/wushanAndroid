package cn.ninanina.wushanvideo.ui.video;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arialyy.aria.core.Aria;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.common.util.CollectionUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.SingleVideoListAdapter;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoClickListener;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoOptionClickListener;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.video.Tag;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.util.DBHelper;
import cn.ninanina.wushanvideo.util.DialogManager;
import cn.ninanina.wushanvideo.util.FileUtil;
import me.gujun.android.taggroup.TagGroup;

public class DetailFragment extends Fragment {
    @BindView(R.id.video_detail_title)
    TextView titleTextView;
    @BindView(R.id.video_detail_info)
    TextView infoTextView;
    @BindView(R.id.video_detail_collect_button)
    ConstraintLayout collectButton;
    @BindView(R.id.video_detail_collect_img)
    ImageView collectImg;
    @BindView(R.id.video_detail_collect_num)
    TextView collectNum;
    @BindView(R.id.video_detail_download_button)
    ConstraintLayout downloadButton;
    @BindView(R.id.video_detail_download_img)
    ImageView downloadImg;
    @BindView(R.id.video_detail_download_num)
    TextView downloadNum;
    @BindView(R.id.video_detail_dislike_button)
    ConstraintLayout dislikeButton;
    @BindView(R.id.video_detail_dislike_img)
    ImageView dislikeImg;
    @BindView(R.id.video_detail_dislike_num)
    TextView dislikeNum;
    @BindView(R.id.video_detail_tags)
    TagGroup videoTags;
    @BindView(R.id.detail_related_videos)
    RecyclerView relatedVideos;

    private VideoDetail videoDetail;
    private boolean downloadEnabled = false;
    private boolean downloaded;
    private Long downloadTaskId;
    private boolean disliked;

    YoYo.YoYoString downloadAnimation;

    List<Object> dataList = new ArrayList<>();

    public DetailFragment(VideoDetail videoDetail) {
        super();
        this.videoDetail = videoDetail;
        this.videoDetail.setViewed(this.videoDetail.getViewed() / 100);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        MainActivity.getInstance().setDetailFragment(DetailFragment.this);

        StringBuilder strViewed = new StringBuilder();
        int viewed = videoDetail.getViewed();
        if (viewed >= 10000) {
            int wan = viewed / 10000;
            int qian = viewed % 10000 / 1000;
            strViewed.append(wan);
            if (qian != 0) strViewed.append('.').append(qian);
            strViewed.append("万");
        } else strViewed.append(viewed);
        strViewed.append("播放");
        infoTextView.setText(strViewed.toString());
        StringBuilder titleBuilder = new StringBuilder(videoDetail.getTitle());
        if (!StringUtils.isEmpty(videoDetail.getTitleZh()))
            titleBuilder.append("（机翻：").append(videoDetail.getTitleZh()).append("）");
        titleTextView.setText(titleBuilder.toString());
        List<String> strTags = new ArrayList<>();
        for (Tag tag : videoDetail.getTags()) {
            if (!StringUtils.isEmpty(tag.getTagZh())) strTags.add(tag.getTagZh());
            else strTags.add(tag.getTag());
        }
        videoTags.setTags(strTags);

        relatedVideos.setLayoutManager(new LinearLayoutManager(getContext()));
        if (CollectionUtils.isEmpty(dataList))
            VideoPresenter.getInstance().getRelatedVideos(this, videoDetail.getId());
        else
            relatedVideos.setAdapter(new SingleVideoListAdapter(dataList, new DefaultVideoClickListener(getContext()), new DefaultVideoOptionClickListener(getContext())));

        DBHelper dbHelper = new DBHelper(MainActivity.getInstance());
        if (dbHelper.downloaded(videoDetail.getId())) downloaded = true;
        if (DataHolder.getInstance().collectedVideo(videoDetail.getId()))
            collectImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang_clicked));
        if (downloadEnabled) enableDownload();
        if (downloaded) {
            downloadImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.download_clicked));
            downloadNum.setText("已下载");
            downloadNum.setTextColor(getContext().getColor(R.color.tabColor));
        }

        if (disliked)
            downloadImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dislike_clicked));
        bindEvents();
    }

    private void bindEvents() {
        collectButton.setOnClickListener(collectListener);
        downloadButton.setOnClickListener(downloadListener);
        dislikeButton.setOnClickListener(dislikeListener);
    }

    View.OnClickListener collectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!WushanApp.loggedIn()) {
                DialogManager.getInstance().newLoginDialog(getContext()).show();
                return;
            }
            YoYo.with(Techniques.RubberBand)
                    .duration(500)
                    .playOn(collectImg);
            DialogManager.getInstance().newCollectDialog(getContext(), videoDetail, DataHolder.getInstance().getPlaylists()).show();
        }
    };

    View.OnClickListener downloadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!downloadEnabled) return;
            if (!WushanApp.loggedIn()) {
                DialogManager.getInstance().newLoginDialog(getContext()).show();
                return;
            }
            if (downloaded) {
                if (downloadTaskId != null) {
                    Toast.makeText(getContext(), "暂停下载，点击继续", Toast.LENGTH_SHORT).show();
                    Aria.download(this)
                            .load(downloadTaskId)
                            .stop();
                    downloadAnimation.stop();
                    downloaded = false;
                } else {
                    Toast.makeText(getContext(), "已经下载过了", Toast.LENGTH_SHORT).show();
                }
            } else {
                downloaded = true;
                if (downloadTaskId == null) {
                    int perm = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (perm == PackageManager.PERMISSION_DENIED)
                        ActivityCompat.requestPermissions(getActivity(), new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        }, 0);
                    perm = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (perm == PackageManager.PERMISSION_DENIED)
                        ActivityCompat.requestPermissions(getActivity(), new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                        }, 0);
                    Toast.makeText(getContext(), "开始下载视频", Toast.LENGTH_SHORT).show();
                    downloadNum.setText("0%");
                    downloadAnimation = YoYo.with(Techniques.FadeInDown)
                            .duration(1000)
                            .repeat(9999)
                            .playOn(downloadImg);
                    String src = ((VideoDetailActivity) getActivity()).getSrc();
                    String fileName = videoDetail.getTitle().trim() + ".mp4";
                    fileName = fileName.replaceAll("/", "\0");
                    downloadTaskId = Aria.download(MainActivity.getInstance())
                            .load(src)     //读取下载地址
                            .setFilePath(FileUtil.getVideoDir().getAbsolutePath() + "/" + fileName) //设置文件保存的完整路径
                            .create();   //启动下载
                    DBHelper dbHelper = new DBHelper(MainActivity.getInstance());
                    dbHelper.saveVideo(videoDetail);
                } else {
                    Toast.makeText(getContext(), "继续下载", Toast.LENGTH_SHORT).show();
                    downloadAnimation.stop(true);
                    Aria.download(this)
                            .load(downloadTaskId)
                            .resume();
                }

            }
        }
    };

    View.OnClickListener dislikeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!WushanApp.loggedIn()) {
                DialogManager.getInstance().newLoginDialog(getContext()).show();
                return;
            }
            YoYo.with(Techniques.Tada)
                    .duration(500)
                    .playOn(dislikeImg);
            if (disliked) {
                dislikeImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dislike_enabled));
                dislikeNum.setTextColor(getResources().getColor(R.color.tabColor, getContext().getTheme()));
                disliked = false;
            } else {
                dislikeImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dislike_clicked));
                dislikeNum.setTextColor(getResources().getColor(R.color.black, getContext().getTheme()));
                disliked = true;
            }
        }
    };

    public RecyclerView getRelatedRecyclerView() {
        return relatedVideos;
    }

    public List<Object> getDataList() {
        return dataList;
    }

    public void enableDownload() {
        if (downloaded) return;
        downloadEnabled = true;
        downloadImg.setImageResource(R.drawable.download_enabled);
        downloadNum.setTextColor(getContext().getColor(R.color.tabColor));
    }

    public void finishDownload() {
        downloaded = true;
        downloadTaskId = null;
        downloadAnimation.stop();
        downloadImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.download_clicked));
        downloadNum.setText("已下载");
    }

    //刷新收藏状态
    public void refreshCollect() {
        if (!DataHolder.getInstance().collectedVideo(videoDetail.getId())) {
            collectImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang_enabled));
        } else {
            collectImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shoucang_clicked));
        }
    }

    public TextView getDownloadNum() {
        return downloadNum;
    }

}
