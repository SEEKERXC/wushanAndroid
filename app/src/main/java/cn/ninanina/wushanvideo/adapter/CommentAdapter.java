package cn.ninanina.wushanvideo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.listener.ReplyCommentListener;
import cn.ninanina.wushanvideo.model.bean.common.User;
import cn.ninanina.wushanvideo.model.bean.video.Comment;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.util.TimeUtil;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<Comment> commentList;
    ReplyCommentListener replyListener;

    public CommentAdapter(List<Comment> commentList, ReplyCommentListener replyListener) {
        this.commentList = commentList;
        this.replyListener = replyListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CommentHolder commentHolder = (CommentHolder) holder;
        Comment comment = commentList.get(position);
        User user = comment.getUser();
        commentHolder.cover.setImageURI(user.getPhoto());
        commentHolder.nickname.setText(user.getNickname());
        commentHolder.time.setText(TimeUtil.getFullTime(comment.getTime()));
        commentHolder.content.setText(comment.getContent());
        commentHolder.likeNum.setText(String.valueOf(comment.getApprove()));
        commentHolder.dislikeNum.setText(String.valueOf(comment.getDisapprove()));
        if (comment.getApproved()) commentHolder.likeIcon.setImageResource(R.drawable.like_clicked);
        if (comment.getDisapproved())
            commentHolder.dislikeIcon.setImageResource(R.drawable.dislike_clicked);
        commentHolder.likeButton.setOnClickListener(v -> {
            commentHolder.likeNum.setText(comment.getApprove() + 1);
            commentHolder.likeIcon.setImageResource(R.drawable.like_clicked);
            VideoPresenter.getInstance().approveComment(comment);
            if (comment.getDisapproved()) {
                commentHolder.dislikeNum.setText(comment.getDisapprove() - 1);
                commentHolder.dislikeIcon.setImageResource(R.drawable.dislike);
                VideoPresenter.getInstance().disapproveComment(comment);
            }
        });
        commentHolder.dislikeButton.setOnClickListener(v -> {
            commentHolder.dislikeNum.setText(comment.getDisapprove() + 1);
            commentHolder.dislikeIcon.setImageResource(R.drawable.dislike_clicked);
            VideoPresenter.getInstance().disapproveComment(comment);
            if (comment.getApproved()) {
                commentHolder.likeNum.setText(comment.getApprove() - 1);
                commentHolder.likeIcon.setImageResource(R.drawable.like);
                VideoPresenter.getInstance().approveComment(comment);
            }
        });
        commentHolder.itemView.setOnClickListener(v -> {
            replyListener.onCommentListener(comment);
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void insert(List<Comment> comments) {
        commentList.addAll(comments);
        notifyDataSetChanged();
    }

    public void insert(Comment comment) {
        commentList.add(0, comment);
        notifyDataSetChanged();
    }

    static class CommentHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.photo)
        SimpleDraweeView cover;
        @BindView(R.id.nickname)
        TextView nickname;
        @BindView(R.id.level)
        ImageView level;
        @BindView(R.id.time)
        TextView time;
        @BindView(R.id.content)
        TextView content;
        @BindView(R.id.like_button)
        LinearLayout likeButton;
        @BindView(R.id.like_icon)
        ImageView likeIcon;
        @BindView(R.id.like_num)
        TextView likeNum;
        @BindView(R.id.dislike_button)
        LinearLayout dislikeButton;
        @BindView(R.id.dislike_icon)
        ImageView dislikeIcon;
        @BindView(R.id.dislike_num)
        TextView dislikeNum;
        @BindView(R.id.option)
        FrameLayout option;

        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
