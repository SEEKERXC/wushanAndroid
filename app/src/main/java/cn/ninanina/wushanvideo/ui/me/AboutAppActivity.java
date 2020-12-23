package cn.ninanina.wushanvideo.ui.me;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.githang.statusbar.StatusBarCompat;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.ui.home.SearchActivity;

public class AboutAppActivity extends AppCompatActivity {
    @BindView(R.id.back)
    FrameLayout back;
    @BindView(R.id.btn_article_1)
    TextView btn_1;
    @BindView(R.id.article_1)
    TextView article_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(android.R.color.white, null), true);
        ButterKnife.bind(this);
        bindEvents();
    }

    private void bindEvents() {
        back.setOnClickListener(v -> AboutAppActivity.this.finish());
        btn_1.setOnClickListener(v -> {
            if (article_1.getVisibility() != View.VISIBLE)
                article_1.setVisibility(View.VISIBLE);
            else article_1.setVisibility(View.GONE);
        });
    }
}