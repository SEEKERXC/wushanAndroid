package cn.ninanina.wushanvideo.adapter.listener;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import cn.ninanina.wushanvideo.model.bean.video.Comment;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.ui.video.CommentFragment;
import cn.ninanina.wushanvideo.util.ToastUtil;

public class CommentLongClickListener implements CommentClickListener {
    public CommentLongClickListener(CommentFragment commentFragment) {
        this.commentFragment = commentFragment;
    }

    private CommentFragment commentFragment;

    @Override
    public void onClick(Comment comment) {
        ClipboardManager clipboard = (ClipboardManager) MainActivity.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, comment.getContent());
        clipboard.setPrimaryClip(clipData);
        ToastUtil.show("已复制内容");
    }
}
