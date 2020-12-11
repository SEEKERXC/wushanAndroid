package cn.ninanina.wushanvideo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.listener.TagClickListener;
import cn.ninanina.wushanvideo.model.bean.video.Tag;

public class TagSuggestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public TagSuggestAdapter(List<Tag> suggest, TagClickListener listener, RecyclerView recyclerView) {
        this.suggest = suggest;
        this.listener = listener;
        this.recyclerView = recyclerView;
    }

    List<Tag> suggest;
    TagClickListener listener;
    RecyclerView recyclerView;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SuggestHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_suggest, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SuggestHolder suggestHolder = (SuggestHolder) holder;
        suggestHolder.tag.setText(suggest.get(position).getTagZh());
        suggestHolder.count.setText(String.valueOf(suggest.get(position).getVideoCount()));
        suggestHolder.itemView.setOnClickListener(v -> {
            recyclerView.setVisibility(View.GONE);
            listener.onTagClicked(suggest.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return suggest.size();
    }

    static final class SuggestHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tag)
        TextView tag;
        @BindView(R.id.count)
        TextView count;

        public SuggestHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
