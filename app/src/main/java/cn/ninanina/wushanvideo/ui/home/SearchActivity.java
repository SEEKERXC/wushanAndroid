package cn.ninanina.wushanvideo.ui.home;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.githang.statusbar.StatusBarCompat;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.SearchResultAdapter;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoClickListener;
import cn.ninanina.wushanvideo.adapter.listener.DefaultVideoOptionClickListener;
import cn.ninanina.wushanvideo.model.bean.common.VideoDuration;
import cn.ninanina.wushanvideo.model.bean.common.VideoSortBy;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.util.LayoutUtil;

public class SearchActivity extends AppCompatActivity {
    @BindView(R.id.search_edit)
    EditText searchEdit;
    @BindView(R.id.search_cancel)
    TextView cancel;
    @BindView(R.id.selector)
    public LinearLayout selector;
    @BindView(R.id.search_rank_button)
    ConstraintLayout rankButton;
    @BindView(R.id.search_rank)
    TextView rankText;
    @BindView(R.id.search_rank_icon)
    ImageView rankIcon;
    @BindView(R.id.search_duration_button)
    ConstraintLayout durationButton;
    @BindView(R.id.search_duration)
    TextView durationText;
    @BindView(R.id.search_duration_icon)
    ImageView durationIcon;
    @BindView(R.id.content)
    LinearLayout content;
    @BindView(R.id.swipe)
    public SwipeRefreshLayout swipe;
    @BindView(R.id.search_result)
    RecyclerView recyclerView;

    InputMethodManager imm;
    List<VideoSortBy> sortBys = new ArrayList<VideoSortBy>() {{
        add(VideoSortBy.DEFAULT);
        add(VideoSortBy.VIEWED);
        add(VideoSortBy.COLLECT);
        add(VideoSortBy.DOWNLOAD);
    }};
    List<FrameLayout> rankViews = new ArrayList<>();
    PopupWindow rankPopup;
    List<VideoDuration> durations = new ArrayList<VideoDuration>() {{
        add(VideoDuration.ALL);
        add(VideoDuration.SHORT);
        add(VideoDuration.MIDDLE);
        add(VideoDuration.LONG);
    }};
    List<FrameLayout> durationViews = new ArrayList<>();
    PopupWindow durationPopup;


    public String query;
    public int page = 0;
    public int size = 20;
    public VideoSortBy sortBy = VideoSortBy.DEFAULT;
    public boolean loading = false;
    public boolean loadingFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        StatusBarCompat.setStatusBarColor(SearchActivity.this, getResources().getColor(android.R.color.white, null), true);
        ButterKnife.bind(this);
        initPopup();
        initSearch();
        WushanApp.getInstance().addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WushanApp.getInstance().removeActivity(this);
    }

    private void initSearch() {
        imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        cancel.setOnClickListener(v -> {
//            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            this.finish();
        });
        searchEdit.requestFocus();
        searchEdit.postDelayed(() -> imm.showSoftInput(searchEdit, 0), 50);
        searchEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (loading || loadingFinished) return true;
                //关闭软键盘
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                query = searchEdit.getText().toString().trim();
                if (StringUtils.isEmpty(query))
                    return true;
                page = 0;
                VideoPresenter.getInstance().searchForVideo(this, true);
                return true;
            }
            return false;
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SearchResultAdapter(new ArrayList<>(), new DefaultVideoClickListener(this), new DefaultVideoOptionClickListener(this)));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //列表中LastVisibleItem为倒数第二行时，加载更多
                if (manager.findLastVisibleItemPosition() + 1 >= manager.getItemCount() && !loading && !loadingFinished && !StringUtils.isEmpty(query)) {
                    page++;
                    VideoPresenter.getInstance().searchForVideo(SearchActivity.this, false);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        swipe.setOnRefreshListener(() -> {
            page = 0;
            VideoPresenter.getInstance().searchForVideo(SearchActivity.this, true);
        });
    }

    private void initPopup() {
        View rankView = LayoutInflater.from(this).inflate(R.layout.popup_video_rank, null, false);
        rankViews.add(rankView.findViewById(R.id.rank_default));
        rankViews.add(rankView.findViewById(R.id.rank_play));
        rankViews.add(rankView.findViewById(R.id.rank_collect));
        rankViews.add(rankView.findViewById(R.id.rank_download));

        rankPopup = new PopupWindow(rankView, ViewGroup.LayoutParams.MATCH_PARENT, LayoutUtil.dip2px(this, 50));
        rankPopup.setOutsideTouchable(true);
        rankPopup.setFocusable(true);
        rankPopup.setTouchModal(true);
        View durationView = LayoutInflater.from(this).inflate(R.layout.popup_video_duration, null, false);
        durationViews.add(durationView.findViewById(R.id.duration_all));
        durationViews.add(durationView.findViewById(R.id.duration_short));
        durationViews.add(durationView.findViewById(R.id.duration_middle));
        durationViews.add(durationView.findViewById(R.id.duration_long));

        durationPopup = new PopupWindow(durationView, ViewGroup.LayoutParams.MATCH_PARENT, LayoutUtil.dip2px(this, 50));
        durationPopup.setOutsideTouchable(true);
        durationPopup.setOutsideTouchable(true);
        durationPopup.setFocusable(true);
        durationPopup.setTouchModal(true);

        rankButton.setOnClickListener(v -> {
            if (!rankPopup.isShowing()) {
                rankPopup.showAsDropDown(rankButton);
                rankIcon.setImageResource(R.drawable.up);
                content.setAlpha(0.5f);
            }
        });

        rankPopup.setOnDismissListener(() -> {
            rankIcon.setImageResource(R.drawable.down);
            content.setAlpha(1.0f);
        });

        durationButton.setOnClickListener(v -> {
            if (!durationPopup.isShowing()) {
                durationPopup.showAsDropDown(durationButton);
                durationIcon.setImageResource(R.drawable.up);
                content.setAlpha(0.5f);
            }
        });

        durationPopup.setOnDismissListener(() -> {
            durationIcon.setImageResource(R.drawable.down);
            content.setAlpha(1.0f);
        });

        for (int i = 0; i < rankViews.size(); i++) {
            FrameLayout frameLayout = rankViews.get(i);
            TextView textView = (TextView) frameLayout.getChildAt(0);
            VideoSortBy thisSortBy = sortBys.get(i);
            frameLayout.setOnClickListener(v -> {
                rankPopup.dismiss();
                if (sortBy == thisSortBy) return;
                sortBy = thisSortBy;
                rankText.setText(sortBy.getMsg());
                for (FrameLayout frameLayout1 : rankViews) {
                    TextView textView1 = (TextView) frameLayout1.getChildAt(0);
                    textView1.setTextColor(getColor(R.color.tabColor));
                    textView1.setBackgroundColor(getColor(android.R.color.transparent));
                }
                textView.setBackgroundColor(getColor(R.color.buttonColor));
                textView.setTextColor(getColor(android.R.color.white));
                page = 0;
                VideoPresenter.getInstance().searchForVideo(this, true);
            });
        }

        for (int i = 0; i < durationViews.size(); i++) {
            FrameLayout frameLayout = durationViews.get(i);
            TextView textView = (TextView) frameLayout.getChildAt(0);
            VideoDuration videoDuration = durations.get(i);
            frameLayout.setOnClickListener(v -> {
                durationPopup.dismiss();
                SearchResultAdapter adapter = (SearchResultAdapter) recyclerView.getAdapter();
                if (adapter.getDuration() == videoDuration) return;
                adapter.setDuration(videoDuration);
                durationText.setText(videoDuration.getMsg());
                for (FrameLayout frameLayout1 : durationViews) {
                    TextView textView1 = (TextView) frameLayout1.getChildAt(0);
                    textView1.setTextColor(getColor(R.color.tabColor));
                    textView1.setBackgroundColor(getColor(android.R.color.transparent));
                }
                textView.setBackgroundColor(getColor(R.color.buttonColor));
                textView.setTextColor(getColor(android.R.color.white));
            });
        }
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

}