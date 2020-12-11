package cn.ninanina.wushanvideo.adapter.listener;

import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;
import cn.ninanina.wushanvideo.util.LayoutUtil;
import cn.ninanina.wushanvideo.util.TimeUtil;

public class PlayerTouchListener implements View.OnTouchListener {
    public PlayerTouchListener(ConstraintLayout parentView, StyledPlayerView playerView, VideoDetail videoDetail) {
        player = (SimpleExoPlayer) playerView.getPlayer();
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topToTop = ConstraintSet.PARENT_ID;
        layoutParams.bottomToBottom = ConstraintSet.PARENT_ID;
        layoutParams.startToStart = ConstraintSet.PARENT_ID;
        layoutParams.endToEnd = ConstraintSet.PARENT_ID;
        progress = new TextView(parentView.getContext());
        progress.setLayoutParams(layoutParams);
        progress.setTextSize(14);
        progress.setPadding(10, 6, 10, 6);
        progress.setTextColor(parentView.getContext().getColor(android.R.color.white));
        progress.setBackgroundColor(parentView.getContext().getColor(R.color.shadow));
        progress.setVisibility(View.GONE);
        parentView.addView(progress);
        this.parentView = parentView;
        this.videoDetail = videoDetail;
    }

    private ConstraintLayout parentView;
    private VideoDetail videoDetail;
    private SimpleExoPlayer player;
    float downX;
    float downPosition;
    TextView progress;

    //从屏幕最左边划到最右边，前进一半的时间
    int screenWidth = LayoutUtil.getScreenWidth();
    long duration;
    long seekTo;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //将按下时的坐标存储
                downX = x;
                downPosition = player.getCurrentPosition();
                duration = player.getDuration();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = x;
                float delta = dx - downX;
                if (delta < 10) return true;
                float rate = delta / (screenWidth * 2);
                seekTo = (long) (downPosition + rate * duration);
                if (seekTo <= 0) seekTo = 0;
                if (seekTo >= duration) seekTo = duration;
                progress.setVisibility(View.VISIBLE);
                progress.setText(TimeUtil.getDuration(seekTo) + " / " + TimeUtil.getDuration(duration));
                break;
            case MotionEvent.ACTION_UP:
                progress.setVisibility(View.GONE);
                if (Math.abs(x - downX) < 10) {
                    v.performClick();
                } else if (Math.abs(x - downX) > 60) {
                    player.seekTo(seekTo);
                }
        }
        return true;
    }
}
