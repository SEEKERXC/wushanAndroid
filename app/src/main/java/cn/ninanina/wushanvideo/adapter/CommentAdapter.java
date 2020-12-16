package cn.ninanina.wushanvideo.adapter;

import android.graphics.Color;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.listener.CommentLongClickListener;
import cn.ninanina.wushanvideo.adapter.listener.DefaultCommentClickListener;
import cn.ninanina.wushanvideo.model.bean.common.User;
import cn.ninanina.wushanvideo.model.bean.video.Comment;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.util.TimeUtil;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<Comment> commentList;
    DefaultCommentClickListener replyListener;
    CommentLongClickListener longClickListener;

    public CommentAdapter(List<Comment> commentList, DefaultCommentClickListener replyListener, CommentLongClickListener longClickListener) {
        this.commentList = commentList;
        this.replyListener = replyListener;
        this.longClickListener = longClickListener;
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
        if (comment.getParent() != null) {
            SpannableString spannableString = new SpannableString("回复 @" + comment.getParent().getUser().getNickname() + " :" + comment.getContent());
            spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), 3, comment.getParent().getUser().getNickname().length() + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            commentHolder.content.setText(spannableString);
        } else {
            commentHolder.content.setText(comment.getContent());
        }
        commentHolder.likeNum.setText(String.valueOf(comment.getApprove()));
        commentHolder.dislikeNum.setText(String.valueOf(comment.getDisapprove()));
        if (comment.getApproved())
            commentHolder.likeIcon.setImageResource(R.drawable.like_clicked);
        else commentHolder.likeIcon.setImageResource(R.drawable.like);
        if (comment.getDisapproved())
            commentHolder.dislikeIcon.setImageResource(R.drawable.dislike_clicked);
        else commentHolder.dislikeIcon.setImageResource(R.drawable.dislike);
        commentHolder.likeButton.setOnClickListener(v -> VideoPresenter.getInstance().approveComment(CommentAdapter.this, position));
        commentHolder.dislikeButton.setOnClickListener(v -> VideoPresenter.getInstance().disapproveComment(CommentAdapter.this, position));
        commentHolder.holder.setOnClickListener(v -> replyListener.onClick(comment));
        commentHolder.holder.setOnLongClickListener(v -> {
            longClickListener.onClick(comment);
            return true;
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

    public void update(Comment comment, int position) {
        commentList.set(position, comment);
        notifyDataSetChanged();
    }

    public List<Comment> getCommentList() {
        return commentList;
    }

    static final class CommentHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.photo)
        SimpleDraweeView cover;
        @BindView(R.id.holder)
        LinearLayout holder;
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
