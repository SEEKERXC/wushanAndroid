package cn.ninanina.wushanvideo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.model.bean.common.VideoOption;

public class VideoOptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<VideoOption> videoOptions;
    List<View.OnClickListener> listeners;

    public VideoOptionAdapter(List<VideoOption> videoOptions, List<View.OnClickListener> listeners) {
        this.videoOptions = videoOptions;
        this.listeners = listeners;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoOptionHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_option, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VideoOption videoOption = videoOptions.get(position);
        VideoOptionHolder videoOptionHolder = (VideoOptionHolder) holder;
        videoOptionHolder.icon.setImageResource(videoOption.getResourceId());
        videoOptionHolder.text.setText(videoOption.getName());
        videoOptionHolder.itemView.setOnClickListener(listeners.get(position));
    }

    @Override
    public int getItemCount() {
        return videoOptions.size();
    }

    static final class VideoOptionHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.video_option_icon)
        ImageView icon;
        @BindView(R.id.video_option_text)
        TextView text;

        public VideoOptionHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
