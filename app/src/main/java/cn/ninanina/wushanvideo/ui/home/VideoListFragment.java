package cn.ninanina.wushanvideo.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.network.VideoPresenter;

public class VideoListFragment extends Fragment {
    @BindView(R.id.video_list)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_video_list)
    SwipeRefreshLayout swipeRefreshLayout;

    public VideoListFragment(String type) {
        this.type = type;
    }

    private String type;

    private boolean isLoading = false;
    public final int size = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_video_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        swipeRefreshLayout.setColorSchemeResources(R.color.tabColor);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isLoading)
                VideoPresenter.getInstance().getRecommendVideoList(this, VideoPresenter.RecyclerViewOp.SWIPE);
            else Toast.makeText(getContext(), "正在加载，请稍等~", Toast.LENGTH_SHORT).show();
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //列表中LastVisibleItem为倒数第二行时，加载更多
                if (manager.findLastCompletelyVisibleItemPosition() + 1 >= manager.getItemCount() && !isLoading) {
                    VideoPresenter.getInstance().getRecommendVideoList(VideoListFragment.this, VideoPresenter.RecyclerViewOp.APPEND);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        //TODO:加载完视频列表并显示完后，加载相关视频。
        VideoPresenter.getInstance().getRecommendVideoList(this, VideoPresenter.RecyclerViewOp.INIT);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public String getType() {
        return type;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

}
