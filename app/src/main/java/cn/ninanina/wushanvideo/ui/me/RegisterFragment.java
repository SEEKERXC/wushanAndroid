package cn.ninanina.wushanvideo.ui.me;

import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.network.CommonPresenter;
import cn.ninanina.wushanvideo.util.DialogManager;

public class RegisterFragment extends Fragment {
    @BindView(R.id.root)
    LinearLayout root;
    @BindView(R.id.register_username)
    EditText usernameEdit;
    @BindView(R.id.register_password)
    EditText passwordEdit;
    @BindView(R.id.register_nickname)
    EditText nicknameEdit;
    @BindView(R.id.register_gender)
    RadioGroup genderButton;
    @BindView(R.id.register_username_ok)
    ImageView usernameOKIcon;
    @BindView(R.id.register_username_exists)
    TextView usernameExists;
    @BindView(R.id.register_password_ok)
    ImageView passwordOKIcon;
    @BindView(R.id.register_nickname_ok)
    ImageView nicknameOKIcon;
    @BindView(R.id.register_button)
    Button registerButton;
    @BindView(R.id.protocol)
    LinearLayout protocol;

    private boolean usernameOK;
    private boolean passwordOK;
    private boolean genderOK;
    private int gender;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
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
                if (s.toString().length() >= 6 && s.toString().length() <= 16) {
                    CommonPresenter.getInstance().checkUsernameExists(s.toString(), RegisterFragment.this);
                } else {
                    usernameOK = false;
                    usernameOKIcon.setVisibility(View.INVISIBLE);
                    usernameExists.setVisibility(View.INVISIBLE);
                }
                showHideRegister();
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
                if (s.toString().length() >= 6 && s.toString().length() <= 16) {
                    passwordOK = true;
                    passwordOKIcon.setVisibility(View.VISIBLE);
                } else {
                    passwordOK = false;
                    passwordOKIcon.setVisibility(View.INVISIBLE);
                }
                showHideRegister();
            }
        });
        genderButton.setOnCheckedChangeListener((group, checkedId) -> {
            genderOK = true;
            gender = checkedId;
            showHideRegister();
        });

        registerButton.setOnClickListener(v -> {
            InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (im != null) {
                im.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
            }
            CommonPresenter.getInstance().register(RegisterFragment.this);
        });
        protocol.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProtocolActivity.class);
            startActivity(intent);
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

    private void showHideRegister() {
        if (usernameOK && passwordOK && genderOK) {
            registerButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.buttonClickColor, null));
            registerButton.setEnabled(true);
        } else {
            registerButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), android.R.color.transparent, null));
            registerButton.setEnabled(false);
        }
    }

    public ImageView getUsernameOKIcon() {
        return usernameOKIcon;
    }

    public TextView getUsernameExists() {
        return usernameExists;
    }

    public void setUsernameOK(boolean usernameOK) {
        this.usernameOK = usernameOK;
    }

    public EditText getUsernameEdit() {
        return usernameEdit;
    }

    public EditText getPasswordEdit() {
        return passwordEdit;
    }

    public EditText getNicknameEdit() {
        return nicknameEdit;
    }

    public int getGender() {
        return gender;
    }

}
