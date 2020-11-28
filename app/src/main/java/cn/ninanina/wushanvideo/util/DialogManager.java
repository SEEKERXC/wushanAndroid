package cn.ninanina.wushanvideo.util;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flyco.dialog.widget.MaterialDialog;
import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.PlaylistAdapter;
import cn.ninanina.wushanvideo.adapter.VideoOptionAdapter;
import cn.ninanina.wushanvideo.adapter.listener.CollectPlaylistClickListener;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.common.VideoOption;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.ui.me.LoginActivity;

/**
 * 管理对话框。
 * 同一时间只能显示一个对话框
 */
public class DialogManager {

    private static DialogManager instance = new DialogManager();

    private DialogManager() {
    }

    public static DialogManager getInstance() {
        return instance;
    }

    private AlertDialog newDirDialog;

    /**
     * 收藏列表对话框，点击项目收藏视频
     */
    public AlertDialog newCollectDialog(Context context, VideoDetail videoDetail, List<Playlist> playlists) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.dialog_list, null, false);
        RecyclerView recyclerView = frameLayout.findViewById(R.id.playlists);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        AlertDialog collectDialog = new AlertDialog.Builder(context)
                .setTitle("收藏视频")
                .setIcon(R.drawable.directory)
                .setView(frameLayout)
                .create();
        if (!CollectionUtils.isEmpty(playlists))
            recyclerView.setAdapter(new PlaylistAdapter(playlists, videoDetail, new CollectPlaylistClickListener(context, videoDetail, collectDialog)));
        else
            Toast.makeText(context, "还没有收藏夹", Toast.LENGTH_SHORT).show();
        return collectDialog;
    }

    /**
     * 视频选项对话框
     */
    public AlertDialog newVideoOptionDialog(Context context, VideoDetail videoDetail) {
        AlertDialog videoOptionDialog;
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.dialog_list, null, false);
        videoOptionDialog = new AlertDialog.Builder(context)
                .setView(frameLayout)
                .create();
        RecyclerView recyclerView = frameLayout.findViewById(R.id.playlists);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        List<VideoOption> videoOptions = new ArrayList<VideoOption>() {{
            add(new VideoOption(R.drawable.video, "添加至稍后看"));
            add(new VideoOption(R.drawable.shoucang, "收藏"));
            add(new VideoOption(R.drawable.download_1296db, "下载"));
            add(new VideoOption(R.drawable.dislike, "不喜欢"));
        }};
        List<View.OnClickListener> listeners = new ArrayList<View.OnClickListener>() {{
            add(v -> {
                Toast.makeText(context, "已添加到稍后看列表", Toast.LENGTH_SHORT).show();
                videoOptionDialog.dismiss();
            });
            add(v -> {
                newCollectDialog(context, videoDetail, DataHolder.getInstance().getPlaylists()).show();
                videoOptionDialog.dismiss();
            });
            add(v -> {
                Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show();
                videoOptionDialog.dismiss();
            });
            add(v -> {
                Toast.makeText(context, "将减少此类视频推荐", Toast.LENGTH_SHORT).show();
                videoOptionDialog.dismiss();
            });
        }};
        recyclerView.setAdapter(new VideoOptionAdapter(videoOptions, listeners));
        return videoOptionDialog;
    }

    /**
     * 创建收藏夹对话框
     */
    public AlertDialog newCreatePlaylistDialog(Context context, RecyclerView list) {
        newDirDialog = new AlertDialog.Builder(context)
                .setTitle("新建收藏夹")
                .setIcon(R.drawable.new_directory)
                .setView(R.layout.dialog_new_collect)
                .setPositiveButton("完成", (dialog, which) -> {
                    EditText editText = newDirDialog.findViewById(R.id.collect_name);
                    assert editText != null;
                    String name = editText.getText().toString();
                    if (name.length() <= 0) return;
                    VideoPresenter.getInstance().createPlaylist(context, list, name);
                })
                .create();
        newDirDialog.setOnShowListener(dialog -> {
            EditText editText = newDirDialog.findViewById(R.id.collect_name);
            if (editText != null) {
                editText.requestFocus();
                editText.postDelayed(() -> {
                    InputMethodManager inputManager = (InputMethodManager) editText
                            .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(editText, 0);
                }, 50);
            }
        });
        return newDirDialog;
    }

    public MaterialDialog newLoginDialog(Context context) {
        MaterialDialog loginDialog = new MaterialDialog(context).title("请先登录")
                .content("客官还没登录哦~ 登录后，我们会为您提供更好的服务")
                .btnNum(2)
                .btnText("先逛逛", "马上登录")
                .btnTextSize(12.0f, 15.0f)
                .btnTextColor(R.color.black, R.color.tabColor);
        loginDialog.setOnBtnClickL(null, () -> {
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
        });
        return loginDialog;
    }

}
