package cn.ninanina.wushanvideo.adapter.listener;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import java.util.ArrayList;
import java.util.List;

import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;
import cn.ninanina.wushanvideo.util.LayoutUtil;
import cn.ninanina.wushanvideo.util.TimeUtil;

public class PlayerTouchListener implements View.OnTouchListener {
    public PlayerTouchListener(VideoDetailActivity activity) {
        player = (SimpleExoPlayer) activity.detailPlayer.getPlayer();
        bottomController = activity.bottomController;
        this.activity = activity;
        bottomShadow = activity.bottomShadow;
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topToTop = ConstraintSet.PARENT_ID;
        layoutParams.bottomToBottom = ConstraintSet.PARENT_ID;
        layoutParams.startToStart = ConstraintSet.PARENT_ID;
        layoutParams.endToEnd = ConstraintSet.PARENT_ID;
        progress = new TextView(activity);
        progress.setLayoutParams(layoutParams);
        progress.setTextSize(14);
        progress.setPadding(10, 6, 10, 6);
        progress.setTextColor(activity.getColor(android.R.color.white));
        progress.setBackgroundColor(activity.getColor(R.color.shadow));
        progress.setVisibility(View.GONE);
        activity.parent.addView(progress);
    }

    private VideoDetailActivity activity;
    private LinearLayout bottomController;
    private View bottomShadow;
    private SimpleExoPlayer player;
    float downX;
    float downPosition;
    TextView progress;

    //从屏幕最左边划到最右边，前进一半的时间
    int screenWidth = LayoutUtil.getScreenWidth();
    long duration;
    long seekTo;

    long lastClickTime = 0;
    float lastClickX = 0;
    float lastClickY = 0;

    Runnable closeControllerTask;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //将按下时的坐标存储
                downX = x;
                downPosition = player.getCurrentPosition();
                duration = player.getDuration();
                break;
            case MotionEvent.ACTION_MOVE:
                float delta = x - downX;
                if (Math.abs(delta) < 60) return true;
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
                    long nowTime = System.currentTimeMillis();
                    if (nowTime - lastClickTime < 400 && Math.abs(x - lastClickX) < 50 && Math.abs(y - lastClickY) < 50) {
                        //double click
                        if (activity.isFullScreen)
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        else
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        lastClickTime = 0;
                        lastClickX = 0;
                        lastClickY = 0;
                    } else {
                        //single click
                        if (bottomController.getVisibility() == View.VISIBLE) {
                            bottomController.setVisibility(View.INVISIBLE);
                            bottomShadow.setVisibility(View.INVISIBLE);
                            bottomController.removeCallbacks(closeControllerTask);
                        } else {
                            bottomController.setVisibility(View.VISIBLE);
                            bottomShadow.setVisibility(View.VISIBLE);
                            //3000ms later to hide the controller
                            closeControllerTask = () -> {
                                if (bottomController.getVisibility() == View.VISIBLE) {
                                    bottomController.setVisibility(View.INVISIBLE);
                                    bottomShadow.setVisibility(View.INVISIBLE);
                                }
                            };
                            bottomController.postDelayed(closeControllerTask, 3000);
                        }
                    }
                    lastClickTime = nowTime;
                    lastClickX = x;
                    lastClickY = y;
                } else if (Math.abs(x - downX) > 60) {
                    player.seekTo(seekTo);
                }
        }
        return true;
    }
}
