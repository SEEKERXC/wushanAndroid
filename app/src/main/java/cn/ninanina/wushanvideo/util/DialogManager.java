package cn.ninanina.wushanvideo.util;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.flyco.dialog.widget.MaterialDialog;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ListHolder;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.adapter.CollectItemAdapter;
import cn.ninanina.wushanvideo.adapter.VideoOptionAdapter;
import cn.ninanina.wushanvideo.model.bean.common.VideoOptionItem;
import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import cn.ninanina.wushanvideo.model.bean.video.VideoDir;
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

    private DialogPlus videoOptionDialog;
    private DialogPlus collectDialog;
    private MaterialDialog loginDialog;
    private AlertDialog newDirDialog;

    private List<VideoOptionItem> videoOptionItems = new ArrayList<VideoOptionItem>() {{
        add(new VideoOptionItem(R.drawable.video, "添加至稍后看"));
        add(new VideoOptionItem(R.drawable.shoucang, "收藏"));
        add(new VideoOptionItem(R.drawable.xiazai_clicked, "下载"));
        add(new VideoOptionItem(R.drawable.dislike, "不喜欢"));
    }};

    //标记被点击的videoOptionItem
    private int videoOptionClickedIndex = -1;
    //大于0表示optionDialog隐藏结束
    private int optionDialogDismissed = 0;
    //大于0表示collectDialog创建完成
    private int collectDialogFinished = 0;

    public Handler handler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageType.OPTION_DIALOG_DISMISSED:
                    optionDialogDismissed++;
                    break;
                case MessageType.COLLECT_DIALOG_FINISHED:
                    collectDialogFinished++;
                    break;
            }
            if (collectDialogFinished > 0 && optionDialogDismissed > 0) {
                collectDialog.show();
                optionDialogDismissed--;
                collectDialogFinished--;
            }
        }
    };

    /**
     * 收藏列表对话框
     */
    public void newCollectDialog(Context context, VideoDetail videoDetail, List<VideoDir> videoDirs) {
        ListView listView = (ListView) LayoutInflater.from(context).inflate(R.layout.dialog_collect_dir_list, null, false);
        listView.setAdapter(new CollectItemAdapter(context, R.layout.item_collect_dir, videoDirs));
        ConstraintLayout header = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.header_dialog_collect, null, false);
        collectDialog = DialogPlus.newDialog(context)
                .setContentHolder(new ViewHolder(listView))
                .setHeader(header)
                .setExpanded(true)
                .create();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            VideoDir dir = videoDirs.get(position);
            VideoPresenter.getInstance().collectVideo(context, videoDetail.getId(), dir.getId());
            collectDialog.dismiss();
        });
    }

    /**
     * 视频选项对话框
     */
    public DialogPlus newVideoOptionDialog(Context context, VideoDetail videoDetail) {
        if (loginDialog == null) {
            loginDialog = new MaterialDialog(context).title("请先登录")
                    .content("客官还没登录哦~ 登录后，我们会为您提供更好的服务")
                    .btnNum(2)
                    .btnText("先逛逛", "马上登录")
                    .btnTextSize(12.0f, 15.0f)
                    .btnTextColor(R.color.black, R.color.tabColor);
            loginDialog.setOnBtnClickL(null, () -> {
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
            });
        }
        videoOptionDialog = DialogPlus.newDialog(context)
                .setContentHolder(new ListHolder())
                .setAdapter(new VideoOptionAdapter(context, R.layout.item_video_option, videoOptionItems))
                .setPadding(0, 0, 0, 10)
                .setExpanded(false)
                .setOnItemClickListener((dialog1, item, view, position) -> {
                    dialog1.dismiss();
                    videoOptionClickedIndex = position;
                    if (!WushanApp.loggedIn()) {
                        loginDialog.show();
                        videoOptionClickedIndex = -1;
                        return;
                    }
                    switch (position) {
                        case 0:
                            break;
                        case 1:
                            VideoPresenter.getInstance().getVideoDirsForDialog(context, videoDetail);
                            break;
                        case 3:
                        case 4:
                    }
                })
                .setOnDismissListener(dialog -> {
                    switch (videoOptionClickedIndex) {
                        case 1: //点击的是收藏
                            Message message = new Message();
                            message.what = MessageType.OPTION_DIALOG_DISMISSED;
                            handler.sendMessage(message);
                    }
                    videoOptionClickedIndex = -1;
                })
                .create();
        return videoOptionDialog;
    }

    /**
     * 创建收藏夹对话框
     */
    public AlertDialog newCreateDirDialog(Context context, ListView listView) {
        newDirDialog = new AlertDialog.Builder(context)
                .setTitle("新建收藏夹")
                .setIcon(R.drawable.new_directory)
                .setView(R.layout.dialog_new_collect)
                .setPositiveButton("完成", (dialog, which) -> {
                    EditText editText = newDirDialog.findViewById(R.id.collect_name);
                    if (editText != null) {
                        String name = editText.getText().toString();
                        if (name.length() <= 0) return;
                        VideoPresenter.getInstance().createVideoDir(context, listView, name);
                    }
                })
                .create();
        newDirDialog.setOnShowListener(dialog -> {
            EditText editText = newDirDialog.findViewById(R.id.collect_name);
            if (editText != null) {
                editText.requestFocus();
                Handler handler = editText.getHandler();
                handler.postDelayed(() -> {
                    InputMethodManager inputManager = (InputMethodManager) editText
                            .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(editText, 0);
                }, 100);
            }
        });
        return newDirDialog;
    }

    public MaterialDialog getLoginDialog() {
        return loginDialog;
    }

    /**
     * 定义消息类型
     */
    public static class MessageType {
        public static final int COLLECT_DIALOG_FINISHED = 1000; //收藏夹dialog创建完成
        public static final int OPTION_DIALOG_DISMISSED = 1001;//视频选项dialog隐藏完成
    }

}
