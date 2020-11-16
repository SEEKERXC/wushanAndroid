package cn.ninanina.wushanvideo.network;

import android.content.Context;
import android.content.SharedPreferences;
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
import cn.ninanina.wushanvideo.ui.me.RegisterFragment;
import cn.ninanina.wushanvideo.util.EncodeUtil;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
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
            System.out.println(currentMinute);
            String secret = EncodeUtil.encodeSHA(currentMinute + "jdfohewk");
            getCommonService().genAppkey(secret)
                    .subscribeOn(Schedulers.io())
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
                .doOnError(throwable -> Toast.makeText(fragment.getContext(), "网络出错，请稍后重试", Toast.LENGTH_SHORT).show())
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
        SharedPreferences profile = WushanApp.getProfile();
        SharedPreferences.Editor editor = profile.edit();
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
                .doOnError(throwable ->
                        Toast.makeText(WushanApp.getInstance().getApplicationContext(), "网络出错，请稍后试试", Toast.LENGTH_SHORT).show())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userResult -> {
                    String code = userResult.getRspCode();
                    if (code.equals(ResultMsg.APPKEY_INVALID.getCode())) {

                    } else if (code.equals(ResultMsg.USER_EXIST.getCode())) {
                        Toast.makeText(fragment.getContext(), "用户已存在，请换个账号", Toast.LENGTH_SHORT).show();
                    } else if (code.equals(ResultMsg.SUCCESS.getCode())) {
                        User user = userResult.getData();
                        if (user != null) {
                            Toast.makeText(fragment.getContext(), "注册成功！", Toast.LENGTH_SHORT).show();
                            editor.putLong("userId", user.getId());
                            editor.putString("username", user.getUsername());
                            editor.putString("password", user.getPassword());
                            editor.putString("nickname", user.getNickname());
                            editor.putInt("userAge", user.getAge());
                            editor.putLong("registerTime", user.getRegisterTime());
                            editor.putLong("lastLoginTime", user.getLastLoginTime());
                            editor.apply();
                            Objects.requireNonNull(fragment.getActivity()).finish();
                        }
                    }
                });
    }

    /**
     * 检查登录状态，保证开启应用的时候处于登录
     */
    public void checkForLogin() {
        String appKey = WushanApp.getAppKey();
        long userId = WushanApp.getProfile().getLong("userId", 0);
        getCommonService().checkLogin(appKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getRspCode().equals(ResultMsg.NOT_LOGIN.getCode())) login();
                    else if (result.getRspCode().equals(ResultMsg.SUCCESS.getCode())) {
                        if (userId != result.getData().getId()){
                            //TODO:检查登录返回的user与本地保存的user不一致，错误处理，低优先级
                        }
                    }
                });
    }

    private void login() {
        String appKey = WushanApp.getAppKey();
        SharedPreferences profile = WushanApp.getProfile();
        SharedPreferences.Editor editor = profile.edit();
        String username = profile.getString("username", "");
        String password = profile.getString("password", "");
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password))
            getCommonService().login(appKey, username, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(userResult -> {
                        editor.putLong("lastLoginTime", userResult.getData().getLastLoginTime());
                        editor.apply();
                    });
    }

    public static CommonPresenter getInstance() {
        return COMMON;
    }
}
