package cn.ninanina.wushanvideo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.listener.TagClickListener;
import cn.ninanina.wushanvideo.model.bean.video.Tag;

public class TagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<Tag> tags;
    boolean showChinese = true;
    TagClickListener listener;

    public TagAdapter(List<Tag> tags, TagClickListener listener) {
        this.tags = tags;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TagHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_detail, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TagHolder tagHolder = (TagHolder) holder;
        Tag tag = tags.get(position);
        if (showChinese)
            tagHolder.name.setText(tag.getTagZh());
        else tagHolder.name.setText(tag.getTag());
        tagHolder.count.setText(tag.getVideoCount() + "个视频");
        tagHolder.cover.setImageURI(tag.getCover());
        tagHolder.itemView.setOnClickListener(v -> {
            listener.onTagClicked(tag);
        });
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public void insert(List<Tag> newTags) {
        this.tags.addAll(newTags);
        notifyDataSetChanged();
    }

    public void changeLanguage() {
        showChinese = !showChinese;
        notifyItemRangeChanged(0, tags.size());
    }

    public boolean isShowChinese() {
        return showChinese;
    }

    static class TagHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tag_cover)
        SimpleDraweeView cover;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.count)
        TextView count;

        public TagHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
