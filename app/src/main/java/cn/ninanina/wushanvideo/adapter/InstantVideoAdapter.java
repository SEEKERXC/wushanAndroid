package cn.ninanina.wushanvideo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.listener.DownloadClickListener;
import cn.ninanina.wushanvideo.adapter.listener.VideoClickListener;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.util.CommonUtils;
import cn.ninanina.wushanvideo.util.DialogManager;
import cn.ninanina.wushanvideo.util.ToastUtil;

public class InstantVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Object> dataList;

    private VideoClickListener videoClickListener;
    private DownloadClickListener downloadListener;
    private View.OnClickListener likeListener;

    private final int typeVideo = 0;

    public InstantVideoAdapter(Context context, List<Object> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == typeVideo) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_instant, parent, false);
            return new InstantVideoHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case typeVideo:
                VideoDetail videoDetail = (VideoDetail) dataList.get(position);
                InstantVideoHolder videoHolder = (InstantVideoHolder) holder;
                videoHolder.title.setText(StringUtils.isEmpty(videoDetail.getTitleZh()) ? videoDetail.getTitle() : videoDetail.getTitleZh());

                SimpleExoPlayer player = new SimpleExoPlayer.Builder(context).build();
                if (CommonUtils.isSrcValid(videoDetail.getSrc())) {
                    MediaItem mediaItem = MediaItem.fromUri(videoDetail.getSrc());
                    player.setMediaItem(mediaItem);
                    player.prepare();
                } else {
                    VideoPresenter.getInstance().getSrcForInstant(player, videoDetail);
                }
                videoHolder.player.setPlayer(player);
                videoHolder.player.setShowPreviousButton(false);
                videoHolder.player.setShowShuffleButton(false);
                videoHolder.player.setShowFastForwardButton(false);
                videoHolder.player.setShowRewindButton(false);
                videoHolder.player.setShowNextButton(false);
                videoHolder.player.setControllerShowTimeoutMs(1000);
                videoHolder.player.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                videoHolder.middle.setOnClickListener(v -> videoClickListener.onClick(videoDetail));
                videoHolder.collectButton.setOnClickListener(v -> {
                    if (!WushanApp.loggedIn()) {
                        DialogManager.getInstance().newLoginDialog(context).show();
                        return;
                    }
                    DialogManager.getInstance().newCollectDialog(context, videoDetail).show();
                });
                videoHolder.downloadButton.setOnClickListener(v -> {
                    if (!WushanApp.loggedIn()) {
                        DialogManager.getInstance().newLoginDialog(context).show();
                        return;
                    }
                    downloadListener.onClick(videoDetail);
                });
                final boolean[] liked = {DataHolder.getInstance().likedVideo(videoDetail.getId())};
                boolean disliked = DataHolder.getInstance().dislikedVideo(videoDetail.getId());
                if (liked[0])
                    videoHolder.likeImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.like_clicked));
                if (disliked)
                    videoHolder.dislikeImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.dislike_clicked));
                videoHolder.likeButton.setOnClickListener(v -> {
                    if (!WushanApp.loggedIn()) {
                        DialogManager.getInstance().newLoginDialog(context).show();
                        return;
                    }
                    liked[0] = !liked[0];
                    VideoPresenter.getInstance().likeVideo(context, videoDetail.getId());
                    if (liked[0])
                        videoHolder.likeImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.like_clicked));
                    else
                        videoHolder.likeImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.like));
                });
                videoHolder.dislikeButton.setOnClickListener(v -> {
                    player.release();
                    int index = dataList.indexOf(videoDetail);
                    dataList.remove(videoDetail);
                    notifyItemRemoved(index);
                    ToastUtil.show("将减少类似推荐");
                    VideoPresenter.getInstance().dislikeVideo(videoDetail.getId());
                });
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    @Override
    public int getItemViewType(int position) {
        Object o = dataList.get(position);
        if (o instanceof VideoDetail) return typeVideo;
        return super.getItemViewType(position);
    }

    public void insert(List<Object> data) {
        int count = dataList.size();
        dataList.addAll(data);
        notifyItemRangeInserted(count, data.size());
    }

    public List<Object> getDataList() {
        return dataList;
    }

    public InstantVideoAdapter setDownloadListener(DownloadClickListener downloadListener) {
        this.downloadListener = downloadListener;
        return this;
    }

    public InstantVideoAdapter setLikeListener(View.OnClickListener likeListener) {
        this.likeListener = likeListener;
        return this;
    }

    public InstantVideoAdapter setVideoClickListener(VideoClickListener videoClickListener) {
        this.videoClickListener = videoClickListener;
        return this;
    }

    static final class InstantVideoHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.player)
        StyledPlayerView player;
        @BindView(R.id.middle)
        LinearLayout middle;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.collect_button)
        ConstraintLayout collectButton;
        @BindView(R.id.collect_text)
        TextView collectText;
        @BindView(R.id.download_button)
        ConstraintLayout downloadButton;
        @BindView(R.id.download_text)
        TextView downloadText;
        @BindView(R.id.like_button)
        ConstraintLayout likeButton;
        @BindView(R.id.like_img)
        ImageView likeImg;
        @BindView(R.id.like_text)
        TextView likeText;
        @BindView(R.id.dislike_button)
        ConstraintLayout dislikeButton;
        @BindView(R.id.dislike_img)
        ImageView dislikeImg;
        @BindView(R.id.dislike_text)
        TextView dislikeText;

        public InstantVideoHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
