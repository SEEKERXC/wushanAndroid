package cn.ninanina.wushanvideo.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.adapter.VideoListAdapter;
import cn.ninanina.wushanvideo.model.bean.video.Tag;
import cn.ninanina.wushanvideo.network.VideoListPresenter;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;

public class VideoListFragment extends Fragment {
    @BindView(R.id.video_list)
    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_video_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        new VideoListPresenter(this).getVideoList();
    }

    public void setItems(List<VideoDetail> videoDetails) {
        List<View.OnClickListener> itemListeners = new ArrayList<>();
        for (final VideoDetail videoDetail : videoDetails) {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), VideoDetailActivity.class);
                    intent.putExtra("id", videoDetail.getId());
                    intent.putExtra("title",videoDetail.getTitle());
                    intent.putExtra("viewed", videoDetail.getViewed());
                    intent.putExtra("coverUrl", videoDetail.getCoverUrl());
                    ArrayList<String> tags = new ArrayList<>();
                    for (Tag tag : videoDetail.getTags()) tags.add(tag.getTag());
                    intent.putStringArrayListExtra("tags", tags);
                    startActivity(intent);
                }
            };
            itemListeners.add(listener);
        }
        recyclerView.setAdapter(new VideoListAdapter(videoDetails, itemListeners));
    }

}
