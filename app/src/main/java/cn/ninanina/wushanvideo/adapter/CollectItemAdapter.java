package cn.ninanina.wushanvideo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.model.bean.video.VideoDir;

public class CollectItemAdapter extends ArrayAdapter<VideoDir> {

    public CollectItemAdapter(@NonNull Context context, int resource, @NonNull List<VideoDir> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(super.getContext()).inflate(R.layout.item_collect_dir, null, false);
        SimpleDraweeView cover = view.findViewById(R.id.collect_cover);
        TextView title = view.findViewById(R.id.collect_title);
        TextView info = view.findViewById(R.id.collect_info);
        VideoDir videoDir = getItem(position);
        if (videoDir != null) {
            if (!StringUtils.isEmpty(videoDir.getCover()))
                cover.setImageURI(videoDir.getCover());
            title.setText(videoDir.getName());
            info.setText(videoDir.getCount() + "个视频 · " + (videoDir.getIsPublic() ? "公开" : "私有"));
        }
        return view;
    }
}
