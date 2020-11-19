package cn.ninanina.wushanvideo.ui.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.githang.statusbar.StatusBarCompat;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.VideoListAdapter;
import cn.ninanina.wushanvideo.model.bean.video.Tag;
import cn.ninanina.wushanvideo.network.VideoListPresenter;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;

public class SearchActivity extends AppCompatActivity {
    @BindView(R.id.search_edit)
    EditText searchEdit;
    @BindView(R.id.search_cancel)
    TextView cancel;

    @BindView(R.id.search_result)
    RecyclerView recyclerView;

    private VideoListAdapter.ItemClickListener clickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        StatusBarCompat.setStatusBarColor(SearchActivity.this, getResources().getColor(R.color.white, null), true);
        ButterKnife.bind(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        init();
    }

    private void init() {
        cancel.setOnClickListener(v -> this.finish());
        clickListener = videoDetail -> {
            Intent intent = new Intent(this, VideoDetailActivity.class);
            intent.putExtra("id", videoDetail.getId());
            intent.putExtra("title", videoDetail.getTitle());
            intent.putExtra("titleZh", videoDetail.getTitleZh());
            intent.putExtra("viewed", videoDetail.getViewed());
            intent.putExtra("coverUrl", videoDetail.getCoverUrl());
            ArrayList<String> tags = new ArrayList<>();
            for (Tag tag : videoDetail.getTags()) {
                if (!StringUtils.isEmpty(tag.getTagZh()) && !tags.contains(tag.getTagZh()))
                    tags.add(tag.getTagZh());
                else tags.add(tag.getTag());
            }
            if (videoDetail.getTags().isEmpty()) tags.add("无标签");
            intent.putStringArrayListExtra("tags", tags);
            startActivity(intent);
        };
        searchEdit.setFocusable(true);
        searchEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                //关闭软键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                String query = searchEdit.getText().toString();
                if (query.length() <= 0) return false;
                if (query.length() >= 15) {
                    Toast.makeText(this, "搜索字数请小于15，谢谢！", Toast.LENGTH_SHORT).show();
                    return false;
                }
                VideoListPresenter.getInstance().searchForVideo(this, query, 0, 10);
                return true;
            }
            return false;
        });
    }

    public VideoListAdapter.ItemClickListener getClickListener() {
        return clickListener;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

}