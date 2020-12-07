package cn.ninanina.wushanvideo.ui.video;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.util.DialogManager;

public class CommentFragment extends Fragment {

    public CommentFragment(VideoDetail videoDetail) {
        this.videoDetail = videoDetail;
    }

    private VideoDetail videoDetail;

    @BindView(R.id.comments)
    RecyclerView recyclerView;
    @BindView(R.id.input)
    EditText input;
    @BindView(R.id.publish)
    TextView publish;
    @BindView(R.id.footer)
    LinearLayout footer;

    private InputMethodManager inputMethodManager;

    //当前加载的页数
    private int page = 0;
    public final int size = 20;

    //正在加载
    private boolean isLoading = false;

    //加载完成标识
    private boolean loadComplete = false;

    //排序依据
    private Sort sort = Sort.HOT;

    //软键盘高度
    private int keyboardHeight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_comment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        inputMethodManager = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        keyboardHeight = WushanApp.getProfile().getInt("keyboardHeight", 0);
        Rect outRect = new Rect();
        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) recyclerView.getLayoutParams();
        params.height = outRect.bottom - outRect.top;
        initEvent();
        VideoPresenter.getInstance().loadComments(this);
    }

    private void initEvent() {
        publish.setOnClickListener(v -> {
            if (!WushanApp.loggedIn()) {
                DialogManager.getInstance().newLoginDialog(getContext()).show();
            } else {
                if (!input.getText().toString().isEmpty() && input.getText().toString().length() > 1)
                    VideoPresenter.getInstance().publishComment(this);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //列表中LastVisibleItem为倒数第二行时，加载更多
                if (manager.findLastVisibleItemPosition() + 1 >= manager.getItemCount() && !isLoading && !loadComplete) {
                    page++;
                    VideoPresenter.getInstance().loadComments(CommentFragment.this);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) footer.getLayoutParams();
        //当键盘弹出隐藏的时候会 调用此方法。
        input.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            //获取当前界面可视部分
            if (getActivity() == null) return;
            getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
            //获取屏幕的高度
            int screenHeight = getActivity().getWindow().getDecorView().getRootView().getHeight();
            //此处就是用来获取键盘的高度的， 在键盘没有弹出的时候 此高度为0 键盘弹出的时候为一个正数
            if (screenHeight - r.bottom > 0 && keyboardHeight == 0) {
                keyboardHeight = screenHeight - r.bottom;
                SharedPreferences profile = WushanApp.getProfile();
                SharedPreferences.Editor editor = profile.edit();
                editor.putInt("keyboardHeight", keyboardHeight).apply();
            }
        });
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                layoutParams.bottomMargin = keyboardHeight;
            } else {
                layoutParams.bottomMargin = 0;
            }
        });
    }


    public VideoDetail getVideoDetail() {
        return videoDetail;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public int getPage() {
        return page;
    }

    public Sort getSort() {
        return sort;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public void setLoadComplete(boolean loadComplete) {
        this.loadComplete = loadComplete;
    }

    public EditText getInput() {
        return input;
    }

    public enum Sort {
        HOT("hot", "按热度"),
        TIME("time", "按时间");

        private final String code;
        private final String msg;

        Sort(String code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public String getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
