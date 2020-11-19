package cn.ninanina.wushanvideo.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.VideoListAdapter;
import cn.ninanina.wushanvideo.model.bean.video.Tag;
import cn.ninanina.wushanvideo.network.VideoListPresenter;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;

public class VideoListFragment extends Fragment {
    @BindView(R.id.video_list)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_video_list)
    SwipeRefreshLayout swipeRefreshLayout;

    public VideoListFragment(String type) {
        this.type = type;
    }

    private String type;
    private VideoListAdapter.ItemClickListener clickListener;

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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout.setColorSchemeResources(R.color.tabColor);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            VideoListPresenter.getInstance().getVideoList(this, swipeRefreshLayout, VideoListPresenter.Op.SWIPE, type);
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //列表中LastVisibleItem为倒数第二行时，加载更多
                if (manager.findLastVisibleItemPosition() + 1 >= manager.getItemCount()) {
                    VideoListPresenter.getInstance().getVideoList(VideoListFragment.this, swipeRefreshLayout, VideoListPresenter.Op.APPEND, type);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        clickListener = videoDetail -> {
            Intent intent = new Intent(getActivity(), VideoDetailActivity.class);
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
        VideoListPresenter.getInstance().getVideoList(this, swipeRefreshLayout, VideoListPresenter.Op.INIT, type);
    }

    public VideoListAdapter.ItemClickListener getClickListener() {
        return clickListener;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    //视频列表类型
    private enum Type {
        RECOMMEND, //首页精选
        ASIAN, //首页亚洲
        WEST,//首页欧美
        LESBIAN,//首页女同
        NO_AD,//首页免广告
    }
}
