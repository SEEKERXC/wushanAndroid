package cn.ninanina.wushanvideo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.listener.PlaylistClickListener;
import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.util.DBHelper;

public class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<Playlist> playlists;
    private VideoDetail videoDetail;

    PlaylistClickListener listener;

    PlaylistClickListener longClickListener;

    public boolean showDownloadNum = true;

    public PlaylistAdapter(List<Playlist> playlists, PlaylistClickListener listener) {
        this.playlists = playlists;
        this.listener = listener;
    }

    public PlaylistAdapter(List<Playlist> playlists, VideoDetail videoDetail, PlaylistClickListener listener) {
        this.playlists = playlists;
        this.videoDetail = videoDetail;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaylistHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        if (playlist != null) {
            PlaylistHolder playlistHolder = (PlaylistHolder) holder;
            if (!StringUtils.isEmpty(playlist.getCover()))
                playlistHolder.cover.setImageURI(playlist.getCover());
            playlistHolder.name.setText(playlist.getName());
            StringBuilder infoBuilder = new StringBuilder("");
            infoBuilder.append(playlist.getCount()).append("个视频");
            if (showDownloadNum) {
                int downloadCount = 0;
                DBHelper dbHelper = WushanApp.getInstance().getDbHelper();
                for (VideoDetail videoDetail : playlist.getVideoDetails()) {
                    if (dbHelper.downloaded(videoDetail.getId())) downloadCount++;
                }
                if (downloadCount == 0) infoBuilder.append(" · 未下载");
                else if (downloadCount < playlist.getVideoDetails().size())
                    infoBuilder.append(" · ").append(downloadCount).append("个已下载");
                else infoBuilder.append(" · ").append("全部已下载");
            }
            playlistHolder.info.setText(infoBuilder.toString());
            playlistHolder.itemView.setOnClickListener(v -> listener.onPlaylistClicked(playlist));
            if (longClickListener != null) {
                playlistHolder.action.setVisibility(View.VISIBLE);
                playlistHolder.action.setOnClickListener(v -> longClickListener.onPlaylistClicked(playlist));
                playlistHolder.itemView.setOnLongClickListener(v -> {
                    longClickListener.onPlaylistClicked(playlist);
                    return true;
                });
            }
            if (videoDetail != null && playlist.getVideoDetails().contains(videoDetail)) {
                playlistHolder.finish.setVisibility(View.VISIBLE);
                playlistHolder.finishText.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public void insert(Playlist playlist) {
        playlists.add(0, playlist);
        notifyItemInserted(0);
    }

    public void clear() {
        playlists.clear();
        notifyDataSetChanged();
    }

    public void setLongClickListener(PlaylistClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    static final class PlaylistHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.collect_cover)
        SimpleDraweeView cover;
        @BindView(R.id.collect_title)
        TextView name;
        @BindView(R.id.collect_info)
        TextView info;
        @BindView(R.id.finish)
        ImageView finish;
        @BindView(R.id.finish_text)
        TextView finishText;
        @BindView(R.id.action)
        FrameLayout action;

        public PlaylistHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
