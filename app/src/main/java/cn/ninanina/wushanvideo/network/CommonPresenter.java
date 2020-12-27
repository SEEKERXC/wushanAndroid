package cn.ninanina.wushanvideo.network;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.CollectionUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.DataHolder;
import cn.ninanina.wushanvideo.model.bean.common.Constants;
import cn.ninanina.wushanvideo.model.bean.common.ResultMsg;
import cn.ninanina.wushanvideo.model.bean.common.User;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.ui.me.LoginFragment;
import cn.ninanina.wushanvideo.ui.me.ProfileActivity;
import cn.ninanina.wushanvideo.ui.me.RegisterFragment;
import cn.ninanina.wushanvideo.util.DialogManager;
import cn.ninanina.wushanvideo.util.EncodeUtil;
import cn.ninanina.wushanvideo.util.ToastUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 这个类用于管理APP的profile信息，包括appKey、user相关信息、设置信息等应用级别的key-value值，都保存在WushanApp类的SharedPreferences里面。
 */
public class CommonPresenter extends BasePresenter {
    private static final CommonPresenter COMMON = new CommonPresenter();

    private CommonPresenter() {
    }

    /**
     * 开启activity时调用，保证appKey有效，并且保证profile不为空。
     * 调用之前一定先设置wushanApp
     */
    public void requestForProfile(SharedPreferences profile) {
        String appKey = profile.getString("appKey", "");
        if (StringUtils.isEmpty(appKey)) {
            SharedPreferences.Editor editor = profile.edit();
            long currentMinute = System.currentTimeMillis() / 1000 / 60;
            String secret = EncodeUtil.encodeSHA(currentMinute + Constants.SECRET_KEY);
            getCommonService().genAppkey(secret)
                    .subscribeOn(Schedulers.io())
                    .doOnError(throwable -> {
                        Looper.prepare();
                        ToastUtil.show("网络开小差了~");
                        Looper.loop();
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(stringResult -> {
                        editor.putString("appKey", stringResult.getData());
                        editor.apply();
                    });
        }
    }

    public void checkUsernameExists(String username, RegisterFragment fragment) {
        String appKey = WushanApp.getAppKey();
        ImageView usernameOKIcon = fragment.getUsernameOKIcon();
        TextView usernameExists = fragment.getUsernameExists();
        getCommonService().userExists(appKey, username)
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    ToastUtil.show("网络开小差了~");
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringResult -> {
                    String msgCode = stringResult.getRspCode();
                    if (msgCode.equals(ResultMsg.USER_EXIST.getCode())) {
                        usernameOKIcon.setVisibility(View.INVISIBLE);
                        usernameExists.setVisibility(View.VISIBLE);
                        fragment.setUsernameOK(false);
                    } else if (msgCode.equals(ResultMsg.SUCCESS.getCode())) {
                        usernameOKIcon.setVisibility(View.VISIBLE);
                        usernameExists.setVisibility(View.INVISIBLE);
                        fragment.setUsernameOK(true);
                    }
                });
    }

    public void register(RegisterFragment fragment) {
        DialogManager.getInstance().showPending(fragment.getActivity(), "注册中");
        String appKey = WushanApp.getAppKey();
        int genderCode = fragment.getGender();
        String gender = "";
        switch (genderCode) {
            case R.id.register_gender_male:
                gender = "MALE";
                break;
            case R.id.register_gender_female:
                gender = "FEMALE";
        }
        String username = fragment.getUsernameEdit().getText().toString().trim();
        String password = fragment.getPasswordEdit().getText().toString().trim();
        String nickname = fragment.getNicknameEdit().getText().toString().trim();
        getCommonService().register(appKey, username, password, nickname, gender)
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                            Looper.prepare();
                            ToastUtil.show("网络开小差了~");
                            DialogManager.getInstance().dismissPending();
                            Looper.loop();
                        }
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pairResult -> {
                    String code = pairResult.getRspCode();
                    if (code.equals(ResultMsg.APPKEY_INVALID.getCode())) {
                    } else if (code.equals(ResultMsg.USER_EXIST.getCode())) {
                        ToastUtil.show("用户已存在，请换个账号");
                    } else if (code.equals(ResultMsg.SUCCESS.getCode())) {
                        SharedPreferences.Editor editor = WushanApp.getProfile().edit();
                        editor.putString("token", pairResult.getData().getFirst()).apply();
                        User user = pairResult.getData().getSecond();
                        user.setPassword(password);
                        updateUserProfile(user);
                        WushanApp.getInstance().initData();
                        for (int i = 1; i < 50; i++) {
                            fragment.getPasswordEdit().postDelayed(() -> {
                                if (fragment.getActivity() != null && !CollectionUtils.isEmpty(DataHolder.getInstance().getPlaylists())) {
                                    fragment.getActivity().finish();
                                }
                            }, i * 100);
                        }
                        ToastUtil.show("注册成功！");
                    }
                    DialogManager.getInstance().dismissPending();
                });
    }

    /**
     * 检查登录状态，保证开启应用的时候处于登录
     */
    public void checkForLogin() {
        getCommonService().checkLogin(getAppKey(), getToken())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getRspCode().equals(ResultMsg.NOT_LOGIN.getCode())) login();
                    else WushanApp.getInstance().initData();
                });
    }

    /**
     * 程序自动登录
     */
    private void login() {
        String appKey = WushanApp.getAppKey();
        SharedPreferences profile = WushanApp.getProfile();
        String username = profile.getString("username", "");
        String password = profile.getString("password", "");
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password))
            getCommonService().login(appKey, username, password)
                    .subscribeOn(Schedulers.io())
                    .doOnError(throwable -> {
                        Looper.prepare();
                        Looper.loop();
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(pairResult -> {
                        String resCode = pairResult.getRspCode();
                        if (resCode.equals(ResultMsg.FAILED.getCode())) {
                            ToastUtil.show("登录失败，请手动登录");
                        } else if (resCode.equals(ResultMsg.SUCCESS.getCode())) {
                            SharedPreferences.Editor editor = WushanApp.getProfile().edit();
                            editor.putString("token", pairResult.getData().getFirst()).apply();
                            User user = pairResult.getData().getSecond();
                            user.setPassword(password);
                            updateUserProfile(user);
                            WushanApp.getInstance().initData();
                        }
                    });
    }

    /**
     * 用户主动登录
     */
    public void login(LoginFragment fragment) {
        String appKey = WushanApp.getAppKey();
        String username = fragment.getUsernameEdit().getText().toString().trim();
        String password = fragment.getPasswordEdit().getText().toString().trim();
        DialogManager.getInstance().showPending(fragment.getActivity(), "登录中");
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password))
            getCommonService().login(appKey, username, password)
                    .subscribeOn(Schedulers.io())
                    .doOnError(throwable -> {
                        Looper.prepare();
                        ToastUtil.show("网络开小差了~");
                        Looper.loop();
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(pairResult -> {
                        String resCode = pairResult.getRspCode();
                        if (resCode.equals(ResultMsg.FAILED.getCode())) {
                            ToastUtil.show("登录失败，用户名或密码错误");
                            DialogManager.getInstance().dismissPending();
                        } else if (resCode.equals(ResultMsg.SUCCESS.getCode())) {
                            SharedPreferences.Editor editor = WushanApp.getProfile().edit();
                            editor.putString("token", pairResult.getData().getFirst()).apply();
                            User user = pairResult.getData().getSecond();
                            user.setPassword(password);
                            updateUserProfile(user);
                            WushanApp.getInstance().initData();
                            for (int i = 1; i < 50; i++) {
                                fragment.getPasswordEdit().postDelayed(() -> {
                                    if (fragment.getActivity() != null && !CollectionUtils.isEmpty(DataHolder.getInstance().getPlaylists())) {
                                        DialogManager.getInstance().dismissPending();
                                        fragment.getActivity().finish();
                                    }
                                }, i * 100);
                            }
                            ToastUtil.show("登录成功，欢迎回来！");
                        }
                    });
    }

    public void logout(ProfileActivity activity) {
        SharedPreferences profile = WushanApp.getProfile();
        SharedPreferences.Editor editor = profile.edit();
        getCommonService().logout(getAppKey(), getToken())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    ToastUtil.show("网络开小差了~");
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(nullResult -> {
                    ToastUtil.show("成功退出登录");
                    editor.remove("token").remove("userId").remove("username").remove("password").remove("nickname").remove("photo")
                            .remove("userAge").remove("gender").remove("registerTime").remove("lastLoginTime").apply();
                    activity.finish();
                });
    }

    public void updateUser() {
        SharedPreferences profile = WushanApp.getProfile();
        getCommonService().updateUser(getAppKey(), getToken(),
                profile.getString("gender", ""),
                profile.getString("password", ""),
                profile.getString("nickname", ""),
                profile.getInt("userAge", 0),
                profile.getBoolean("straight", true))
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    ToastUtil.show("网络开小差了~");
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userResult -> {
                    if (userResult.getRspCode().equals(ResultMsg.SUCCESS.getCode())) {
                        ToastUtil.show("操作成功");
                        User user = userResult.getData();
                        user.setPassword(profile.getString("password", ""));
                        updateUserProfile(user);
                        Handler handler = ProfileActivity.handler;
                        if (handler != null) {
                            Message message = new Message();
                            message.what = ProfileActivity.update;
                            handler.sendMessage(message);
                        }
                    } else if (userResult.getRspCode().equals(ResultMsg.NOT_LOGIN.getCode())) {
                        ToastUtil.show("会话已过期，请重新打开APP");
                    }
                });
    }

    /**
     * 检查最新版本
     */
    public void checkVersion(Context context) {
        SharedPreferences constants = WushanApp.getConstants();
        String nowVersion = constants.getString("version", Constants.VERSION);
        getCommonService().getVersion()
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getData() != null) {
                        DataHolder.getInstance().setNewVersion(result.getData());
                    }
                    if (result.getData() != null && !result.getData().getVersionCode().equals(nowVersion)) {
                        DialogManager.getInstance().newUpdateDialog(context, result.getData()).show();
                    }
                });
    }

    /**
     * 提交反馈
     */
    public void sendFeedback(Activity activity, String content) {
        DialogManager.getInstance().showPending(activity, "");
        getCommonService().sendFeedback(getAppKey(), getToken(), content)
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    ToastUtil.show("网络开小差了~");
                    DialogManager.getInstance().dismissPending();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getRspCode().equals(ResultMsg.SUCCESS.getCode()))
                        ToastUtil.show("提交成功");
                    else if (result.getRspCode().equals(ResultMsg.EMPTY_CONTENT.getCode()))
                        ToastUtil.show("空内容错误");
                    else if (result.getRspCode().equals(ResultMsg.NOT_LOGIN.getCode()))
                        ToastUtil.show("会话已过期");
                    DialogManager.getInstance().dismissPending();
                });
    }

    /**
     * 获取联系方式
     */
    public void getContact(TextView textView) {
        getCommonService().getContact(getAppKey())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getRspCode().equals(ResultMsg.SUCCESS.getCode()))
                        textView.setText(result.getData());
                });
    }

    /**
     * 获取用户协议
     */
    public void getProtocol() {
        getCommonService().getProtocol(getAppKey())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getRspCode().equals(ResultMsg.SUCCESS.getCode()))
                        DataHolder.getInstance().setProtocol(result.getData());
                });
    }

    private void updateUserProfile(User user) {
        SharedPreferences profile = WushanApp.getProfile();
        SharedPreferences.Editor editor = profile.edit();
        editor.putLong("userId", user.getId())
                .putString("username", user.getUsername())
                .putString("password", user.getPassword())
                .putString("nickname", user.getNickname())
                .putString("photo", user.getPhoto())
                .putInt("userAge", user.getAge())
                .putString("gender", user.getGender())
                .putBoolean("straight", user.getStraight())
                .putLong("registerTime", user.getRegisterTime())
                .putLong("lastLoginTime", user.getLastLoginTime())
                .apply();
    }

    public static CommonPresenter getInstance() {
        return COMMON;
    }
}
