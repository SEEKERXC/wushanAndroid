package cn.ninanina.wushanvideo.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.model.bean.common.ResultMsg;
import cn.ninanina.wushanvideo.model.bean.common.User;
import cn.ninanina.wushanvideo.ui.MainActivity;
import cn.ninanina.wushanvideo.ui.me.LoginFragment;
import cn.ninanina.wushanvideo.ui.me.ProfileActivity;
import cn.ninanina.wushanvideo.ui.me.RegisterFragment;
import cn.ninanina.wushanvideo.util.EncodeUtil;
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
            String secret = EncodeUtil.encodeSHA(currentMinute + "jdfohewk");
            getCommonService().genAppkey(secret)
                    .subscribeOn(Schedulers.io())
                    .doOnError(throwable -> {
                        Looper.prepare();
                        Toast.makeText(WushanApp.getInstance().getApplicationContext(), "网络开小差了~", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(WushanApp.getInstance().getApplicationContext(), "网络开小差了~", Toast.LENGTH_SHORT).show();
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
        String username = fragment.getUsernameEdit().getText().toString();
        String password = fragment.getPasswordEdit().getText().toString();
        String nickname = fragment.getNicknameEdit().getText().toString();
        getCommonService().register(appKey, username, password, nickname, gender)
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                            Looper.prepare();
                            Toast.makeText(WushanApp.getInstance().getApplicationContext(), "网络开小差了~", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pairResult -> {
                    String code = pairResult.getRspCode();
                    if (code.equals(ResultMsg.APPKEY_INVALID.getCode())) {
                    } else if (code.equals(ResultMsg.USER_EXIST.getCode())) {
                        Toast.makeText(fragment.getContext(), "用户已存在，请换个账号", Toast.LENGTH_SHORT).show();
                    } else if (code.equals(ResultMsg.SUCCESS.getCode())) {
                        SharedPreferences.Editor editor = WushanApp.getProfile().edit();
                        editor.putString("token", pairResult.getData().getFirst()).apply();
                        Toast.makeText(fragment.getContext(), "注册成功！", Toast.LENGTH_SHORT).show();
                        User user = pairResult.getData().getSecond();
                        user.setPassword(password);
                        updateUserProfile(user);
                        Objects.requireNonNull(fragment.getActivity()).finish();
                    }
                });
    }

    /**
     * 检查登录状态，保证开启应用的时候处于登录
     */
    public void checkForLogin(Context context) {
        getCommonService().checkLogin(getAppKey(), getToken())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(WushanApp.getInstance().getApplicationContext(), "网络开小差了~", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getRspCode().equals(ResultMsg.NOT_LOGIN.getCode())) login(context);
                    else MainActivity.getInstance().initData();
                });
    }

    private void login(Context context) {
        String appKey = WushanApp.getAppKey();
        SharedPreferences profile = WushanApp.getProfile();
        String username = profile.getString("username", "");
        String password = profile.getString("password", "");
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password))
            getCommonService().login(appKey, username, password)
                    .subscribeOn(Schedulers.io())
                    .doOnError(throwable -> {
                        Looper.prepare();
                        Toast.makeText(WushanApp.getInstance().getApplicationContext(), "网络开小差了~", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(pairResult -> {
                        String resCode = pairResult.getRspCode();
                        if (resCode.equals(ResultMsg.FAILED.getCode())) {
                            Toast.makeText(context, "登录失败，用户名或密码不对", Toast.LENGTH_SHORT).show();
                        } else if (resCode.equals(ResultMsg.SUCCESS.getCode())) {
                            SharedPreferences.Editor editor = WushanApp.getProfile().edit();
                            editor.putString("token", pairResult.getData().getFirst()).apply();
                            User user = pairResult.getData().getSecond();
                            user.setPassword(password);
                            updateUserProfile(user);
                            MainActivity.getInstance().initData();
                        }
                    });
    }

    public void login(LoginFragment fragment) {
        String appKey = WushanApp.getAppKey();
        String username = fragment.getUsernameEdit().getText().toString();
        String password = fragment.getPasswordEdit().getText().toString();
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password))
            getCommonService().login(appKey, username, password)
                    .subscribeOn(Schedulers.io())
                    .doOnError(throwable -> {
                        Looper.prepare();
                        Toast.makeText(WushanApp.getInstance().getApplicationContext(), "网络开小差了~", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(pairResult -> {
                        String resCode = pairResult.getRspCode();
                        if (resCode.equals(ResultMsg.FAILED.getCode())) {
                            Toast.makeText(fragment.getContext(), "登录失败，用户名或密码不对", Toast.LENGTH_SHORT).show();
                        } else if (resCode.equals(ResultMsg.SUCCESS.getCode())) {
                            SharedPreferences.Editor editor = WushanApp.getProfile().edit();
                            editor.putString("token", pairResult.getData().getFirst()).apply();
                            User user = pairResult.getData().getSecond();
                            user.setPassword(password);
                            updateUserProfile(user);
                            Toast.makeText(fragment.getContext(), "登录成功，欢迎回来！", Toast.LENGTH_SHORT).show();
                            Objects.requireNonNull(fragment.getActivity()).finish();
                        }
                    });
    }

    //TODO:logout
    public void logout(ProfileActivity activity) {
        SharedPreferences profile = WushanApp.getProfile();
        SharedPreferences.Editor editor = profile.edit();
        getCommonService().logout(getAppKey(), getToken())
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Looper.prepare();
                    Toast.makeText(activity, "网络开小差了~", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(nullResult -> {
                    Toast.makeText(activity, "成功退出登录", Toast.LENGTH_SHORT).show();
                    editor.remove("token").remove("userId").remove("username").remove("password").remove("nickname")
                            .remove("userAge").remove("gender").remove("registerTime").remove("lastLoginTime").apply();
                    activity.finish();
                });
    }

    private void updateUserProfile(User user) {
        SharedPreferences profile = WushanApp.getProfile();
        SharedPreferences.Editor editor = profile.edit();
        editor.putLong("userId", user.getId())
                .putString("username", user.getUsername())
                .putString("password", user.getPassword())
                .putString("nickname", user.getNickname())
                .putInt("userAge", user.getAge())
                .putString("gender", user.getGender())
                .putLong("registerTime", user.getRegisterTime())
                .putLong("lastLoginTime", user.getLastLoginTime())
                .apply();
    }

    public static CommonPresenter getInstance() {
        return COMMON;
    }
}
