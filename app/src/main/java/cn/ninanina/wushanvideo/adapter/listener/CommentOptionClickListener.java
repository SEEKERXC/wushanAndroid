package cn.ninanina.wushanvideo.adapter.listener;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.bean.video.Comment;
import cn.ninanina.wushanvideo.ui.video.CommentFragment;
import cn.ninanina.wushanvideo.util.DialogManager;

public class CommentOptionClickListener implements CommentClickListener {
    public CommentOptionClickListener(CommentFragment commentFragment) {
        this.commentFragment = commentFragment;
    }

    private CommentFragment commentFragment;

    @Override
    public void onClick(Comment comment) {
        long myId = WushanApp.getProfile().getLong("userId", 0);
        if (comment.getUser().getId() == myId) {
            DialogManager.getInstance().newMyCommentOptionDialog(commentFragment, comment).show();
        } else {
            DialogManager.getInstance().newOthersCommentOptionDialog(commentFragment, comment).show();
        }
    }
}
