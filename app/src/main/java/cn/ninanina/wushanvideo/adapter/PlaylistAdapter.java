package cn.ninanina.wushanvideo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import cn.ninanina.wushanvideo.adapter.listener.PlaylistClickListener;
import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;

public class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<Playlist> playlists;
    private VideoDetail videoDetail;

    PlaylistClickListener listener;

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
            playlistHolder.info.setText(playlist.getCount() + "个视频 · " + (playlist.getIsPublic() ? "公开" : "私有"));
            playlistHolder.itemView.setOnClickListener(v -> listener.onPlaylistClicked(playlist));
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

        public PlaylistHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
