package cn.ninanina.wushanvideo.adapter.listener;

import android.content.Context;
import android.content.Intent;

import cn.ninanina.wushanvideo.model.bean.video.Tag;
import cn.ninanina.wushanvideo.ui.tag.TagVideoActivity;

public class TagClickListener {
    public TagClickListener(Context context) {
        this.context = context;
    }

    private Context context;

    public void onTagClicked(Tag tag) {
        Intent intent = new Intent(context, TagVideoActivity.class);
        intent.putExtra("tag", tag);
        context.startActivity(intent);
    }
}
