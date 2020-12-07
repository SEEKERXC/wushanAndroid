package cn.ninanina.wushanvideo.ui.tag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.githang.statusbar.StatusBarCompat;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.model.bean.video.Tag;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.ui.home.SearchActivity;
import cn.ninanina.wushanvideo.ui.home.VideoListFragment;

public class TagVideoActivity extends AppCompatActivity {
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.tag_name)
    TextView name;
    @BindView(R.id.option)
    ImageView option;
    @BindView(R.id.info)
    TextView info;
    @BindView(R.id.content)
    RecyclerView content;

    private Tag tag;

    private int page = 0;
    public final int size = 10;
    private Sort sort = Sort.RANDOM;

    private boolean isLoading = false;
    private boolean loadingFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_video);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.white, null), true);
        ButterKnife.bind(this);
        tag = (Tag) getIntent().getSerializableExtra("tag");
        name.setText(tag.getTagZh());
        info.setText(tag.getTag() + "  " + tag.getVideoCount() + "个视频");
        content.setLayoutManager(new GridLayoutManager(this, 2));
        content.setNestedScrollingEnabled(false);
        content.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //列表中LastVisibleItem为倒数第二行时，加载更多
                if (manager.findLastCompletelyVisibleItemPosition() + 1 >= manager.getItemCount() && !isLoading && !loadingFinished) {
                    System.out.println(manager.getItemCount());
                    page++;
                    VideoPresenter.getInstance().getVideosForTag(TagVideoActivity.this);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        back.setOnClickListener(v -> TagVideoActivity.this.finish());
        VideoPresenter.getInstance().getVideosForTag(this);
    }

    public Tag getTag() {
        return tag;
    }

    public Sort getSort() {
        return sort;
    }

    public int getPage() {
        return page;
    }

    public RecyclerView getContent() {
        return content;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public void setLoadingFinished(boolean loadingFinished) {
        this.loadingFinished = loadingFinished;
    }

    public enum Sort {
        HOT("viewed", "按播放数"),
        RANDOM("random", "默认排序"),
        COMMENT("commentNum", "按评论数");

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