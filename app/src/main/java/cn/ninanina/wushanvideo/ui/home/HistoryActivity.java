package cn.ninanina.wushanvideo.ui.home;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.githang.statusbar.StatusBarCompat;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.SingleVideoListAdapter;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoClickListener;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoOptionClickListener;
import cn.ninanina.wushanvideo.model.bean.common.Pair;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.model.bean.video.VideoUserViewed;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.util.TimeUtil;

public class HistoryActivity extends AppCompatActivity {

    @BindView(R.id.scroll)
    NestedScrollView scrollView;
    @BindView(R.id.content)
    LinearLayout content;
    @BindView(R.id.back)
    FrameLayout back;
    @BindView(R.id.calendar)
    ImageView calendar;
    @BindView(R.id.search)
    ImageView search;

    public int page = 0;
    public final int size = 10;
    public long startOfDay = 0;
    private boolean byTimeDesc = true;

    public boolean loading = false;
    public boolean loadingFinished = false;

    private String lastDay = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.white, null), true);
        initEvents();
    }

    private void initEvents() {
        calendar.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this);
            datePickerDialog.show();
        });
        back.setOnClickListener(v -> HistoryActivity.this.finish());
        VideoPresenter.getInstance().getHistoryVideos(this);
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {   //scrollY是滑动的距离
            if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                //滑动到底部
                if (!loading && !loadingFinished) {
                    page++;
                    VideoPresenter.getInstance().getHistoryVideos(this);
                }
            }
        });
    }

    public void showData(List<Pair<VideoUserViewed, VideoDetail>> data) {
        if (content.getChildCount() > 0) content.removeViewAt(content.getChildCount() - 1); //移除加载标语
        for (Pair<VideoUserViewed, VideoDetail> pair : data) {
            String day = TimeUtil.getDate(pair.getFirst().getTime());

            if (day.equals(lastDay)) {
                RecyclerView recyclerView = (RecyclerView) content.getChildAt(content.getChildCount() - 1);
                SingleVideoListAdapter adapter = (SingleVideoListAdapter) recyclerView.getAdapter();
                adapter.insert(pair.getSecond());
            } else {
                TextView textView = new TextView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(25, 20, 10, 20);
                textView.setLayoutParams(layoutParams);
                textView.setText(day);

                RecyclerView recyclerView = new RecyclerView(this);
                LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setLayoutParams(layoutParams1);
                recyclerView.setNestedScrollingEnabled(false);
                recyclerView.setAdapter(new SingleVideoListAdapter(new ArrayList<Object>() {{
                    add(pair.getSecond());
                }}, new DefaultVideoClickListener(this), new DefaultVideoOptionClickListener(this)));
                content.addView(textView);
                content.addView(recyclerView);
            }
            lastDay = day;
        }
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(25, 20, 10, 20);
        textView.setLayoutParams(layoutParams);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        if (loadingFinished) {
            textView.setText("~ 我是有底线的 ~");
        } else {
            textView.setText("加载中...");
        }
        content.addView(textView);
    }
}