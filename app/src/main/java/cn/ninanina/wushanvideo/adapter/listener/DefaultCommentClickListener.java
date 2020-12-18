package cn.ninanina.wushanvideo.adapter.listener;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.bean.video.Comment;
import cn.ninanina.wushanvideo.ui.video.CommentFragment;
import cn.ninanina.wushanvideo.util.DialogManager;

public class DefaultCommentClickListener implements CommentClickListener {
    public DefaultCommentClickListener(CommentFragment commentFragment) {
        this.commentFragment = commentFragment;
    }

    private CommentFragment commentFragment;

    @Override
    public void onClick(Comment comment) {
        long myId = WushanApp.getProfile().getLong("userId", 0);
        if (comment.getUser().getId() == myId) {
            DialogManager.getInstance().newMyCommentOptionDialog(commentFragment, comment).show();
        } else {
            EditText editText = commentFragment.input;
            editText.setHint("回复 @" + comment.getUser().getNickname() + "：");
            editText.requestFocus();
            editText.postDelayed(() -> {
                InputMethodManager inputManager = (InputMethodManager) editText
                        .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText, 0);
            }, 50);
            commentFragment.parentId = comment.getId();
        }
    }
}
