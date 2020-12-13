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
import cn.ninanina.wushanvideo.model.bean.common.Option;

public class OptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<Option> options;
    List<View.OnClickListener> listeners;

    public OptionAdapter(List<Option> options, List<View.OnClickListener> listeners) {
        this.options = options;
        this.listeners = listeners;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OptionHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Option option = options.get(position);
        OptionHolder optionHolder = (OptionHolder) holder;
        optionHolder.icon.setImageResource(option.getResourceId());
        optionHolder.text.setText(option.getName());
        optionHolder.itemView.setOnClickListener(listeners.get(position));
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    static final class OptionHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.video_option_icon)
        ImageView icon;
        @BindView(R.id.video_option_text)
        TextView text;

        public OptionHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
