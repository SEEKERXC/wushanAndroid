package cn.ninanina.wushanvideo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.model.bean.common.VideoOptionItem;

public class VideoOptionAdapter extends ArrayAdapter<VideoOptionItem> {
    public VideoOptionAdapter(@NonNull Context context, int resource, @NonNull List<VideoOptionItem> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = LayoutInflater.from(super.getContext()).inflate(R.layout.item_video_option, null, false);
        ImageView icon = v.findViewById(R.id.video_option_icon);
        TextView name = v.findViewById(R.id.video_option_text);
        VideoOptionItem item = getItem(position);
        assert item != null;
        icon.setImageResource(item.getResourceId());
        name.setText(item.getName());
        return v;
    }
}
