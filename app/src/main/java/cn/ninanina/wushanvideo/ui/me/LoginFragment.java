package cn.ninanina.wushanvideo.ui.me;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.network.CommonPresenter;

public class LoginFragment extends Fragment {
    @BindView(R.id.root)
    LinearLayout root;
    @BindView(R.id.login_username)
    EditText usernameEdit;
    @BindView(R.id.login_password)
    EditText passwordEdit;
    @BindView(R.id.login_forgot)
    TextView forgot;
    @BindView(R.id.login_button)
    Button loginButton;

    private boolean usernameOK;
    private boolean passwordOK;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        init();
    }

    private void init() {
        usernameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                usernameOK = s.toString().length() >= 5 && s.toString().length() <= 16;
                enableOrDisButton();
            }
        });

        passwordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                passwordOK = s.toString().length() >= 5 && s.toString().length() <= 16;
                enableOrDisButton();
            }
        });

        loginButton.setOnClickListener(v -> {
            InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (im != null) {
                im.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
            }
            CommonPresenter.getInstance().login(LoginFragment.this);
        });

        //测量并保存软键盘高度
        root.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            //获取当前界面可视部分
            if (getActivity() == null) return;
            getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
            //获取屏幕的高度
            int screenHeight = getActivity().getWindow().getDecorView().getRootView().getHeight();

            //此处就是用来获取键盘的高度的， 在键盘没有弹出的时候 此高度为0 键盘弹出的时候为一个正数
            if (screenHeight - r.bottom > 0) {
                int keyboardHeight = screenHeight - r.bottom;
                SharedPreferences profile = WushanApp.getProfile();
                SharedPreferences.Editor editor = profile.edit();
                editor.putInt("keyboardHeight", keyboardHeight).apply();
            }
        });
    }

    private void enableOrDisButton() {
        if (usernameOK && passwordOK) loginButton.setEnabled(true);
        else loginButton.setEnabled(false);
    }

    public EditText getUsernameEdit() {
        return usernameEdit;
    }

    public EditText getPasswordEdit() {
        return passwordEdit;
    }

}
