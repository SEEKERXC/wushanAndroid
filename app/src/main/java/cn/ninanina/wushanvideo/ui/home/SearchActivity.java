package cn.ninanina.wushanvideo.ui.home;

import android.app.Activity;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.githang.statusbar.StatusBarCompat;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.network.VideoPresenter;

public class SearchActivity extends AppCompatActivity {
    @BindView(R.id.search_edit)
    EditText searchEdit;
    @BindView(R.id.search_cancel)
    TextView cancel;

    @BindView(R.id.search_result)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        StatusBarCompat.setStatusBarColor(SearchActivity.this, getResources().getColor(android.R.color.white, null), true);
        ButterKnife.bind(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        init();
    }

    private void init() {
        cancel.setOnClickListener(v -> this.finish());
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        searchEdit.requestFocus();
        searchEdit.postDelayed(() -> imm.showSoftInput(searchEdit, 0), 50);
        searchEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                //关闭软键盘
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                String query = searchEdit.getText().toString();
                if (query.length() <= 0) return false;
                if (query.length() >= 15) {
                    Toast.makeText(this, "搜索字数请小于15，谢谢！", Toast.LENGTH_SHORT).show();
                    return false;
                }
                VideoPresenter.getInstance().searchForVideo(this, query, 0, 20);
                return true;
            }
            return false;
        });
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

}