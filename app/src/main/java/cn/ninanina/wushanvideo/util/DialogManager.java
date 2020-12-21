package cn.ninanina.wushanvideo.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flyco.dialog.widget.MaterialDialog;
import com.google.android.exoplayer2.C;
import com.google.android.gms.common.util.CollectionUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.PlaylistAdapter;
import cn.ninanina.wushanvideo.adapter.OptionAdapter;
import cn.ninanina.wushanvideo.adapter.listener.CollectPlaylistClickListener;
import cn.ninanina.wushanvideo.adapter.listener.DefaultDownloadClickListener;
import cn.ninanina.wushanvideo.adapter.listener.VideoClickListener;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.common.Option;
import cn.ninanina.wushanvideo.model.bean.common.Pair;
import cn.ninanina.wushanvideo.model.bean.video.Comment;
import cn.ninanina.wushanvideo.model.bean.video.ToWatch;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.model.bean.video.Playlist;
import cn.ninanina.wushanvideo.model.bean.video.VideoUserViewed;
import cn.ninanina.wushanvideo.network.CommonPresenter;
import cn.ninanina.wushanvideo.network.VideoPresenter;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.ui.home.HistoryActivity;
import cn.ninanina.wushanvideo.ui.home.WatchLaterActivity;
import cn.ninanina.wushanvideo.ui.me.LoginActivity;
import cn.ninanina.wushanvideo.ui.me.MeFragment;
import cn.ninanina.wushanvideo.ui.me.ProfileActivity;
import cn.ninanina.wushanvideo.ui.video.CommentFragment;
import cn.ninanina.wushanvideo.ui.video.VideoDetailActivity;

/**
 * 管理对话框。
 * 同一时间只能显示一个对话框
 */
public class DialogManager {

    private static DialogManager instance = new DialogManager();

    //加载动画
    private PopupWindow loading;

    private DialogManager() {
    }

    public static DialogManager getInstance() {
        return instance;
    }

    /**
     * 收藏列表对话框，点击项目收藏视频
     */
    public Dialog newCollectDialog(Activity activity, VideoDetail videoDetail) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(activity).inflate(R.layout.dialog_list, null, false);
        RecyclerView recyclerView = frameLayout.findViewById(R.id.content);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        //设置最大高度
        frameLayout.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int height = frameLayout.getHeight();
            int maxHeight = LayoutUtil.dip2px(activity, 400);
            if (height > maxHeight) {
                frameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, maxHeight));
                //这里一定是frameLayout，不然会转换异常，由此可见alertdialog底层是frameLayout
            }
        });
        //标题
        ConstraintLayout title = (ConstraintLayout) LayoutInflater.from(activity).inflate(R.layout.dialog_collect_header, null, false);
        ConstraintLayout newFolderBtn = title.findViewById(R.id.new_folder_btn);

        Dialog collectDialog = new AlertDialog.Builder(activity)
                .setCustomTitle(title)
                .setView(frameLayout)
                .create();
        List<Playlist> playlists = DataHolder.getInstance().getPlaylists();
        if (!CollectionUtils.isEmpty(playlists)) {
            PlaylistAdapter adapter = new PlaylistAdapter(playlists, videoDetail, new CollectPlaylistClickListener(activity, videoDetail, collectDialog));
            adapter.showDownloadNum = false;
            recyclerView.setAdapter(adapter);
        } else {
            return newCreatePlaylistDialog(activity, videoDetail);
        }
        newFolderBtn.setOnClickListener(v -> {
            collectDialog.dismiss();
            newCreatePlaylistDialog(activity, videoDetail).show();
        });
        return collectDialog;
    }

    /**
     * 视频选项对话框
     */
    public AlertDialog newVideoOptionDialog(Activity activity, VideoDetail videoDetail) {
        DBHelper dbHelper = WushanApp.getInstance().getDbHelper();
        AlertDialog videoOptionDialog;
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(activity).inflate(R.layout.dialog_list, null, false);
        videoOptionDialog = new AlertDialog.Builder(activity)
                .setView(frameLayout)
                .create();
        RecyclerView recyclerView = frameLayout.findViewById(R.id.content);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        List<Option> options = new ArrayList<Option>() {{
            add(new Option(R.drawable.watch_later, "添加至稍后看"));
            add(new Option(R.drawable.shoucang, "收藏"));
            if (DataHolder.getInstance().dislikedVideo(videoDetail.getId()))
                add(new Option(R.drawable.dislike_clicked, "取消不喜欢"));
            else add(new Option(R.drawable.dislike, "不喜欢"));
        }};
        if (!dbHelper.downloaded(videoDetail.getId()) && !MainActivity.getInstance().downloadService.getTasks().containsKey(videoDetail.getSrc()))
            options.add(new Option(R.drawable.download, "下载"));
        List<View.OnClickListener> listeners = new ArrayList<View.OnClickListener>() {{
            add(v -> {
                videoOptionDialog.dismiss();
                if (!WushanApp.loggedIn()) newLoginDialog(activity).show();
                VideoPresenter.getInstance().addToWatch(activity, videoDetail.getId());
            });
            add(v -> {
                videoOptionDialog.dismiss();
                if (!WushanApp.loggedIn()) newLoginDialog(activity).show();
                newCollectDialog(activity, videoDetail).show();
            });
            add(v -> {
                videoOptionDialog.dismiss();
                if (!WushanApp.loggedIn()) newLoginDialog(activity).show();
                VideoPresenter.getInstance().dislikeVideo(activity, videoDetail);
            });
        }};
        if (options.size() == 4)
            listeners.add(v -> {
                videoOptionDialog.dismiss();
                if (!WushanApp.loggedIn()) newLoginDialog(activity).show();
                ToastUtil.show("加入下载队列");
                VideoPresenter.getInstance().getSrcForDownload(videoDetail);
            });
        recyclerView.setAdapter(new OptionAdapter(options, listeners));
        return videoOptionDialog;
    }

    /**
     * 收藏夹中的视频选项对话框
     */
    public AlertDialog newPlaylistVideoOptionDialog(Activity activity, VideoDetail videoDetail, Playlist playlist) {
        DBHelper dbHelper = WushanApp.getInstance().getDbHelper();
        AlertDialog videoOptionDialog;
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(activity).inflate(R.layout.dialog_list, null, false);
        videoOptionDialog = new AlertDialog.Builder(activity)
                .setView(frameLayout)
                .create();
        RecyclerView recyclerView = frameLayout.findViewById(R.id.content);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        List<Option> options = new ArrayList<>();
        if (!dbHelper.downloaded(videoDetail.getId()) && !MainActivity.getInstance().downloadService.getTasks().containsKey(videoDetail.getSrc()))
            options.add(new Option(R.drawable.download, "下载"));
        options.add(new Option(R.drawable.delete, "取消收藏"));
        options.add(new Option(R.drawable.image, "设为收藏夹封面"));
        List<View.OnClickListener> listeners = new ArrayList<>();
        if (options.size() == 3) {
            listeners.add(v -> {
                videoOptionDialog.dismiss();
                ToastUtil.show("加入下载队列");
                VideoPresenter.getInstance().getSrcForDownload(videoDetail);
            });
        }
        listeners.add(v -> {
            videoOptionDialog.dismiss();
            VideoPresenter.getInstance().cancelCollect(activity, videoDetail, playlist);
        });
        listeners.add(v -> {
            videoOptionDialog.dismiss();
            playlist.setCover(videoDetail.getCoverUrl());
            VideoPresenter.getInstance().updatePlaylist(activity, playlist);
        });
        recyclerView.setAdapter(new OptionAdapter(options, listeners));
        return videoOptionDialog;
    }

    public AlertDialog newRenamePlaylistDialog(Activity activity, Playlist playlist) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(activity).inflate(R.layout.dialog_one_edit, null, false);
        EditText editText = frameLayout.findViewById(R.id.edit);
        AlertDialog newDirDialog = new AlertDialog.Builder(activity)
                .setTitle("重命名")
                .setIcon(R.drawable.edit)
                .setView(frameLayout)
                .setPositiveButton("完成", (dialog, which) -> {
                    assert editText != null;
                    String name = editText.getText().toString().trim();
                    if (name.length() <= 0 || name.length() > 16) return;
                    playlist.setName(name);
                    VideoPresenter.getInstance().updatePlaylist(activity, playlist);
                })
                .setOnDismissListener(dialog -> {
                    InputMethodManager im = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (im != null) {
                        im.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
                    }
                })
                .create();
        newDirDialog.setOnShowListener(dialog -> {
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

    /**
     * 创建收藏夹对话框，如果给定video，则创建后立即收藏
     */
    public AlertDialog newCreatePlaylistDialog(Activity activity, VideoDetail videoDetail) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(activity).inflate(R.layout.dialog_one_edit, null, false);
        EditText editText = frameLayout.findViewById(R.id.edit);
        AlertDialog newDirDialog = new AlertDialog.Builder(activity)
                .setTitle("新建收藏夹")
                .setIcon(R.drawable.new_directory)
                .setView(frameLayout)
                .setPositiveButton("完成", (dialog, which) -> {
                    assert editText != null;
                    String name = editText.getText().toString();
                    if (name.length() <= 0) return;
                    VideoPresenter.getInstance().createPlaylist(activity, name, videoDetail);
                })
                .setOnDismissListener(dialog -> {
                    InputMethodManager im = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (im != null) {
                        im.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
                    }
                })
                .create();
        newDirDialog.setOnShowListener(dialog -> {
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

    /**
     * 登录提示对话框
     */
    public MaterialDialog newLoginDialog(Context context) {
        MaterialDialog loginDialog = new MaterialDialog(context).title("请先登录")
                .content("客官还没登录哦~ 登录后，我们会为您提供更好的服务")
                .btnNum(2)
                .btnText("先逛逛", "马上登录")
                .btnTextSize(12.0f, 15.0f)
                .btnTextColor(Color.GRAY, Color.BLUE);
        loginDialog.setOnBtnClickL(null, () -> {
            loginDialog.dismiss();
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
        });
        return loginDialog;
    }

    /**
     * 清空历史记录对话框
     */
    public MaterialDialog newDeleteAllHistoryDialog(HistoryActivity activity) {
        MaterialDialog dialog = new MaterialDialog(activity)
                .title("确认清空历史记录吗？")
                .titleTextSize(15.0f)
                .btnNum(2)
                .btnText("取消", "确认")
                .btnTextSize(13.0f, 13.0f);
        dialog.setOnBtnClickL(null, () -> {
            dialog.dismiss();
            List<VideoUserViewed> viewed = DataHolder.getInstance().getAllViewed();
            VideoPresenter.getInstance().deleteHistory(viewed, activity, true);

        });
        return dialog;
    }

    /**
     * 收藏夹选项对话框
     */
    public Dialog newPlaylistOptionDialog(Activity activity, Playlist playlist) {
        DBHelper dbHelper = WushanApp.getInstance().getDbHelper();
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(activity).inflate(R.layout.dialog_list, null, false);
        RecyclerView recyclerView = frameLayout.findViewById(R.id.content);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        TextView title = new TextView(activity);
        title.setText("收藏夹: " + playlist.getName());
        title.setPadding(LayoutUtil.dip2px(activity, 20), LayoutUtil.dip2px(activity, 15), LayoutUtil.dip2px(activity, 20), 0);
        title.setTextColor(activity.getColor(android.R.color.black));
        title.setTextSize(16.0f);
        title.setMaxLines(1);
        title.setEllipsize(TextUtils.TruncateAt.END);
        Dialog dialog = new AlertDialog.Builder(activity)
                .setView(frameLayout)
                .setCustomTitle(title)
                .setIcon(R.drawable.shoucang_folder)
                .create();
        List<Option> options = new ArrayList<>();
        List<VideoDetail> toDownload = new ArrayList<>();
        for (VideoDetail videoDetail : playlist.getVideoDetails()) {
            if (dbHelper.downloaded(videoDetail.getId())) continue;
            if (MainActivity.getInstance().downloadService.getTasks().containsKey(videoDetail.getSrc()))
                continue;
            toDownload.add(videoDetail);
        }
        if (toDownload.size() > 0)
            options.add(new Option(R.drawable.download, "下载全部"));
        options.add(new Option(R.drawable.edit, "重命名"));
        if (playlist.getIsPublic()) options.add(new Option(R.drawable.lock, "设为私有"));
        else options.add(new Option(R.drawable.lock, "设为公开"));
        options.add(new Option(R.drawable.delete, "删除"));
        List<View.OnClickListener> listeners = new ArrayList<>();
        if (options.size() == 4) {
            listeners.add(v -> {   //TODO:对下载全部限制点击频率
                dialog.dismiss();
                ToastUtil.show(toDownload.size() + "个视频添加到下载队列");
                for (VideoDetail videoDetail : toDownload) {
                    if (CommonUtils.isSrcValid(videoDetail.getSrc())) {
                        DefaultDownloadClickListener listener = new DefaultDownloadClickListener(MainActivity.getInstance().downloadService);
                        listener.showMessage = false;
                        listener.onClick(videoDetail);
                        continue;
                    }
                    VideoPresenter.getInstance().getSrcForDownload(videoDetail);
                }
            });
        }
        listeners.add(v -> {
            dialog.dismiss();
            newRenamePlaylistDialog(activity, playlist).show();
        });
        listeners.add(v -> {
            dialog.dismiss();
            playlist.setIsPublic(!playlist.getIsPublic());
            VideoPresenter.getInstance().updatePlaylist(activity, playlist);
        });
        listeners.add(v -> {
            dialog.dismiss();
            newDeletePlaylistDialog(activity, playlist).show();
        });
        recyclerView.setAdapter(new OptionAdapter(options, listeners));
        return dialog;
    }

    /**
     * 下载视频列表选项
     */
    public Dialog newDownloadOptionDialog(Context context, VideoDetail videoDetail) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.dialog_list, null, false);
        RecyclerView recyclerView = frameLayout.findViewById(R.id.content);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        TextView title = new TextView(context);
        title.setText("视频: " + (StringUtils.isEmpty(videoDetail.getTitleZh()) ? videoDetail.getTitle() : videoDetail.getTitleZh()));
        title.setPadding(LayoutUtil.dip2px(context, 20), LayoutUtil.dip2px(context, 15), LayoutUtil.dip2px(context, 20), 0);
        title.setTextColor(context.getColor(android.R.color.black));
        title.setTextSize(16.0f);
        title.setMaxLines(1);
        title.setEllipsize(TextUtils.TruncateAt.END);
        Dialog dialog = new AlertDialog.Builder(context)
                .setView(frameLayout)
                .setCustomTitle(title)
                .setIcon(R.drawable.shoucang_folder)
                .create();
        List<Option> options = new ArrayList<Option>() {{
            add(new Option(R.drawable.play_8a8a8a, "详情页"));
            add(new Option(R.drawable.edit, "重命名"));
            add(new Option(R.drawable.delete, "删除"));
            add(new Option(R.drawable.info, "文件信息"));
        }};
        List<View.OnClickListener> listeners = new ArrayList<View.OnClickListener>() {{
            add(v -> {
                dialog.dismiss();
                VideoPresenter.getInstance().getSrcOfOffline(context, videoDetail, videoDetail.getSrc());
            });
            add(v -> {
                dialog.dismiss();
                newRenameDownloadDialog(context, videoDetail).show();
            });
            add(v -> {
                dialog.dismiss();
                newDeleteDownloadDialog(context, videoDetail).show();
            });
            add(v -> {
                dialog.dismiss();
                newFileInfoDialog(context, videoDetail).show();
            });
        }};
        recyclerView.setAdapter(new OptionAdapter(options, listeners));
        return dialog;
    }

    /**
     * 删除下载提示
     */
    public MaterialDialog newDeleteDownloadDialog(Context context, VideoDetail videoDetail) {
        MaterialDialog dialog = new MaterialDialog(context).content("确定删除吗？")
                .contentTextSize(15.0f)
                .btnNum(2)
                .btnText("取消", "确认")
                .btnTextSize(13.0f, 13.0f)
                .btnTextColor(R.color.red, R.color.red);
        dialog.setOnBtnClickL(null, () -> {
            DBHelper dbHelper = WushanApp.getInstance().getDbHelper();
            if (dbHelper.deleteVideo(videoDetail))
                ToastUtil.show("删除成功");
            dialog.dismiss();
        });
        return dialog;
    }

    /**
     * 下载视频文件信息
     */
    public MaterialDialog newFileInfoDialog(Context context, VideoDetail videoDetail) {
        StringBuilder content = new StringBuilder("");
        DBHelper dbHelper = WushanApp.getInstance().getDbHelper();
        File file = new File(FileUtil.getVideoDir() + "/" + dbHelper.getNameById(videoDetail.getId()));
        if (file.exists()) {
            content.append("文件路径：").append(file.getAbsolutePath()).append("\n\n")
                    .append("文件大小：").append(FileUtil.getSize(file.length())).append("\n\n")
                    .append("更新时间：").append(TimeUtil.getFullTime(file.lastModified()));
        }
        return new MaterialDialog(context)
                .title("视频文件信息")
                .titleTextSize(16.0f)
                .content(content.toString())
                .contentTextSize(14.0f)
                .btnNum(1)
                .btnText("OK")
                .btnTextSize(13.0f);
    }

    /**
     * 重命名下载视频文件
     */
    public Dialog newRenameDownloadDialog(Context context, VideoDetail videoDetail) {
        DBHelper dbHelper = WushanApp.getInstance().getDbHelper();
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.dialog_one_edit, null, false);
        EditText editText = frameLayout.findViewById(R.id.edit);
        editText.setHint(dbHelper.getNameById(videoDetail.getId()));
        AlertDialog renameDialog = new AlertDialog.Builder(context)
                .setTitle("重命名")
                .setIcon(R.drawable.edit)
                .setView(frameLayout)
                .setPositiveButton("完成", (dialog, which) -> {
                    assert editText != null;
                    String name = editText.getText().toString().trim();
                    if (name.length() <= 0) return;
                    if (!name.endsWith(".mp4")) name += ".mp4";
                    if (dbHelper.getVideo(name) != null) {
                        ToastUtil.show("已有同名文件");
                        return;
                    }
                    File renameTo = new File(FileUtil.getVideoDir().getAbsolutePath() + "/" + name);
                    if (dbHelper.renameVideo(videoDetail, renameTo))
                        ToastUtil.show("操作成功");
                })
                .setNegativeButton("取消", null)
                .setOnDismissListener(dialog -> {
                    InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (im != null) {
                        im.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
                    }
                })
                .create();
        renameDialog.setOnShowListener(dialog -> {
            if (editText != null) {
                editText.requestFocus();
                editText.postDelayed(() -> {
                    InputMethodManager inputManager = (InputMethodManager) editText
                            .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(editText, 0);
                }, 50);
            }
        });
        return renameDialog;
    }

    /**
     * 历史记录选项
     */
    public Dialog newHistoryOptionDialog(HistoryActivity activity, Pair<VideoUserViewed, VideoDetail> pair) {
        DBHelper dbHelper = WushanApp.getInstance().getDbHelper();
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(activity).inflate(R.layout.dialog_list, null, false);
        RecyclerView recyclerView = frameLayout.findViewById(R.id.content);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        AlertDialog videoOptionDialog = new AlertDialog.Builder(activity)
                .setView(frameLayout)
                .create();
        List<Option> options = new ArrayList<>();
        if (!dbHelper.downloaded(pair.getSecond().getId()) &&
                !MainActivity.getInstance().downloadService.getTasks().containsKey(pair.getSecond().getSrc()))
            options.add(new Option(R.drawable.download, "下载"));
        options.add(new Option(R.drawable.shoucang, "收藏"));
        options.add(new Option(R.drawable.delete, "删除记录"));
        List<View.OnClickListener> listeners = new ArrayList<>();
        if (options.size() == 3) {
            listeners.add(v -> {
                videoOptionDialog.dismiss();
                ToastUtil.show("加入下载队列");
                VideoPresenter.getInstance().getSrcForDownload(pair.getSecond());
            });
        }
        listeners.add(v -> {
            videoOptionDialog.dismiss();
            newCollectDialog(activity, pair.getSecond()).show();
        });
        listeners.add(v -> {
            videoOptionDialog.dismiss();
            VideoPresenter.getInstance().deleteHistory(new ArrayList<VideoUserViewed>() {{
                add(pair.getFirst());
            }}, activity, false);
        });
        recyclerView.setAdapter(new OptionAdapter(options, listeners));
        return videoOptionDialog;
    }

    /**
     * 删除收藏夹提示
     */
    public MaterialDialog newDeletePlaylistDialog(Activity activity, Playlist playlist) {
        MaterialDialog dialog = new MaterialDialog(activity).title("确定删除此收藏夹吗？")
                .titleTextSize(16.0f)
                .content("下载的视频不会删除")
                .contentTextSize(14.0f)
                .btnNum(2)
                .btnText("取消", "确认")
                .btnTextSize(13.0f, 13.0f)
                .btnTextColor(R.color.red, R.color.red);
        dialog.setOnBtnClickL(null, () -> {
            VideoPresenter.getInstance().deletePlaylist(activity, playlist);
            dialog.dismiss();
        });
        return dialog;
    }

    /**
     * 修改密码对话框
     */
    public Dialog newEditPasswordDialog(ProfileActivity profileActivity) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(profileActivity).inflate(R.layout.dialog_two_edit, null, false);
        EditText edit1 = linearLayout.findViewById(R.id.edit1);
        EditText edit2 = linearLayout.findViewById(R.id.edit2);
        edit1.setHint("输入密码（6-16位）");
        edit2.setHint("请确认一次");
        AlertDialog dialog = new AlertDialog.Builder(profileActivity)
                .setTitle("修改密码")
                .setIcon(R.drawable.lock)
                .setView(linearLayout)
                .setPositiveButton("确认", (dialog1, which) -> {
                    String p1 = edit1.getText().toString().trim();
                    String p2 = edit2.getText().toString().trim();
                    if (!p1.equals(p2)) {
                        ToastUtil.show("两次密码不一致");
                        return;
                    }
                    if (p1.length() < 6 || p1.length() > 16) {
                        ToastUtil.show("长度为6-16字符");
                        return;
                    }
                    SharedPreferences.Editor editor = WushanApp.getProfile().edit();
                    editor.putString("password", p1).apply();
                    CommonPresenter.getInstance().updateUser();
                })
                .setNegativeButton("取消", null)
                .setOnDismissListener(d -> {
                    InputMethodManager im = (InputMethodManager) profileActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (im != null) {
                        im.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
                    }
                })
                .create();
        edit1.postDelayed(() -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false), 100);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String word1 = edit1.getText().toString().trim();
                String word2 = edit2.getText().toString().trim();
                if (word1.length() >= 6 && word2.length() >= 6 && word1.equals(word2))
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        };
        edit1.addTextChangedListener(textWatcher);
        edit2.addTextChangedListener(textWatcher);
        dialog.setOnShowListener(d -> {
            edit1.requestFocus();
            edit1.postDelayed(() -> {
                InputMethodManager inputManager = (InputMethodManager) edit1
                        .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(edit1, 0);
            }, 50);
        });
        return dialog;
    }

    /**
     * 修改昵称对话框
     */
    public Dialog newEditNicknameDialog(ProfileActivity profileActivity) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(profileActivity).inflate(R.layout.dialog_one_edit, null, false);
        EditText editText = frameLayout.findViewById(R.id.edit);
        editText.setHint(WushanApp.getProfile().getString("nickname", ""));
        AlertDialog nicknameDialog = new AlertDialog.Builder(profileActivity)
                .setTitle("设置您的昵称")
                .setIcon(R.drawable.nickname)
                .setView(frameLayout)
                .setPositiveButton("完成", (dialog, which) -> {
                    assert editText != null;
                    String name = editText.getText().toString().trim();
                    if (name.length() <= 0) return;
                    SharedPreferences.Editor editor = WushanApp.getProfile().edit();
                    editor.putString("nickname", name).apply();
                    CommonPresenter.getInstance().updateUser();
                })
                .setNegativeButton("取消", null)
                .setOnDismissListener(dialog -> {
                    InputMethodManager im = (InputMethodManager) profileActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (im != null) {
                        im.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
                    }
                })
                .create();
        nicknameDialog.setOnShowListener(dialog -> {
            if (editText != null) {
                editText.requestFocus();
                editText.postDelayed(() -> {
                    InputMethodManager inputManager = (InputMethodManager) editText
                            .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(editText, 0);
                }, 50);
            }
        });
        return nicknameDialog;
    }

    /**
     * 变性对话框
     */
    public Dialog newGenderCheckDialog(ProfileActivity profileActivity) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(profileActivity).inflate(R.layout.dialog_checker, null, false);
        RadioGroup radioGroup = frameLayout.findViewById(R.id.radios);
        AlertDialog genderDialog = new AlertDialog.Builder(profileActivity)
                .setTitle("设置您的性别")
                .setView(frameLayout)
                .create();
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            genderDialog.dismiss();
            SharedPreferences.Editor editor = WushanApp.getProfile().edit();
            switch (checkedId) {
                case R.id.option1:
                    editor.putString("gender", "MALE").apply();
                    break;
                case R.id.option2:
                    editor.putString("gender", "FEMALE").apply();
            }
            CommonPresenter.getInstance().updateUser();
        });
        return genderDialog;
    }

    /**
     * 修改年龄对话框
     */
    public Dialog newEditAgeDialog(ProfileActivity profileActivity) {
        SharedPreferences profile = WushanApp.getProfile();
        SharedPreferences.Editor editor = profile.edit();
        NumberPicker numberPicker = new NumberPicker(profileActivity);
        numberPicker.setMinValue(18);
        numberPicker.setMaxValue(100);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(LayoutUtil.dip2px(profileActivity, 100), LayoutUtil.dip2px(profileActivity, 100));
        numberPicker.setLayoutParams(layoutParams);
        numberPicker.setValue(Math.max(profile.getInt("userAge", 18), 18));
        return new AlertDialog.Builder(profileActivity)
                .setTitle("设置您的年龄")
                .setView(numberPicker)
                .setPositiveButton("确定", (dialog, which) -> {
                    dialog.dismiss();
                    editor.putInt("userAge", numberPicker.getValue()).apply();
                    CommonPresenter.getInstance().updateUser();
                })
                .create();
    }

    /**
     * 修改性取向对话框
     */
    public Dialog newEditOrientationDialog(ProfileActivity profileActivity) {
        SharedPreferences.Editor editor = WushanApp.getProfile().edit();
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(profileActivity).inflate(R.layout.dialog_checker, null, false);
        RadioGroup radioGroup = frameLayout.findViewById(R.id.radios);
        RadioButton option1 = radioGroup.findViewById(R.id.option1);
        RadioButton option2 = radioGroup.findViewById(R.id.option2);
        option1.setText("直");
        option2.setText("弯");
        AlertDialog orientationDialog = new AlertDialog.Builder(profileActivity)
                .setTitle("设置您的取向")
                .setView(frameLayout)
                .create();
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            orientationDialog.dismiss();
            switch (checkedId) {
                case R.id.option1:
                    editor.putBoolean("straight", true).apply();
                    break;
                case R.id.option2:
                    editor.putBoolean("straight", false).apply();
            }
            CommonPresenter.getInstance().updateUser();
        });
        return orientationDialog;
    }

    /**
     * 稍后观看选项对话框
     */
    public Dialog newWatchLaterOptionDialog(WatchLaterActivity activity, Pair<ToWatch, VideoDetail> pair) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(activity).inflate(R.layout.dialog_list, null, false);
        RecyclerView recyclerView = frameLayout.findViewById(R.id.content);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(frameLayout)
                .create();
        List<Option> options = new ArrayList<Option>() {{
            add(new Option(R.drawable.delete, "删除"));
        }};
        List<View.OnClickListener> listeners = new ArrayList<View.OnClickListener>() {{
            add(v -> {
                dialog.dismiss();
                VideoPresenter.getInstance().deleteWatchLater(activity, new ArrayList<Pair<ToWatch, VideoDetail>>() {{
                    add(pair);
                }});
            });
        }};
        recyclerView.setAdapter(new OptionAdapter(options, listeners));
        return dialog;
    }

    /**
     * 自己的评论选项
     */
    public Dialog newMyCommentOptionDialog(CommentFragment commentFragment, Comment comment) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(commentFragment.getContext()).inflate(R.layout.dialog_list, null, false);
        RecyclerView recyclerView = frameLayout.findViewById(R.id.content);
        recyclerView.setLayoutManager(new LinearLayoutManager(commentFragment.getContext()));
        AlertDialog dialog = new AlertDialog.Builder(commentFragment.getContext())
                .setView(frameLayout)
                .create();
        List<Option> options = new ArrayList<Option>() {{
            add(new Option(R.drawable.delete, "删除"));
        }};
        List<View.OnClickListener> listeners = new ArrayList<View.OnClickListener>() {{
            add(v -> {
                dialog.dismiss();
                VideoPresenter.getInstance().deleteComment(commentFragment, comment);
            });
        }};
        recyclerView.setAdapter(new OptionAdapter(options, listeners));
        return dialog;
    }

    /**
     * 别人的评论选项
     */
    public Dialog newOthersCommentOptionDialog(CommentFragment commentFragment, Comment comment) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(commentFragment.getContext()).inflate(R.layout.dialog_list, null, false);
        RecyclerView recyclerView = frameLayout.findViewById(R.id.content);
        recyclerView.setLayoutManager(new LinearLayoutManager(commentFragment.getContext()));
        AlertDialog dialog = new AlertDialog.Builder(commentFragment.getContext())
                .setView(frameLayout)
                .create();
        List<Option> options = new ArrayList<Option>() {{
            add(new Option(R.drawable.info, "举报"));
        }};
        List<View.OnClickListener> listeners = new ArrayList<View.OnClickListener>() {{
            add(v -> {
                dialog.dismiss();
                // TODO: 2020/12/17 0017 举报评论 
            });
        }};
        recyclerView.setAdapter(new OptionAdapter(options, listeners));
        return dialog;
    }

    /**
     * 今天看了很久了，提示一下是否继续观看
     */
    public MaterialDialog newWatchPromptDialog(Context context, VideoDetail videoDetail, VideoClickListener videoClickListener) {
        String todayWatchTime = TimeUtil.getDurationZh(PlayTimeManager.getTodayWatchTime());
        MaterialDialog dialog = new MaterialDialog(context)
                .titleTextSize(16.0f)
                .title("今天已经看了" + todayWatchTime + "，确定继续吗？")
                .titleTextSize(15.0f)
                .content("\n做个自律好宝宝(๑•̀ㅂ•́)و✧\n")
                .contentTextSize(13.0f)
                .btnNum(2)
                .btnText("立即关闭APP", "继续看")
                .btnTextSize(14.0f, 12.0f)
                .btnTextColor(Color.RED, Color.BLUE);
        dialog.setOnBtnClickL(() -> WushanApp.getInstance().onTerminate(), () -> {
            dialog.dismiss();
            videoClickListener.playVideo(videoDetail);
        });
        return dialog;
    }

    /**
     * 显示加载动画
     */
    public void showPending(Activity activity, String text) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.popup_loading, null, false);
        loading = new PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loading.setFocusable(false);
        loading.setOutsideTouchable(false);
        loading.setContentView(linearLayout);
        View root = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        TextView textView = linearLayout.findViewById(R.id.text);
        if (!StringUtils.isEmpty(text)) textView.setText(text);
        loading.showAtLocation(root, Gravity.CENTER, 0, 0);
    }

    public void dismissPending() {
        MainActivity.getInstance().runOnUiThread(() -> {
            if (loading != null)
                loading.dismiss();
        });

    }
}
