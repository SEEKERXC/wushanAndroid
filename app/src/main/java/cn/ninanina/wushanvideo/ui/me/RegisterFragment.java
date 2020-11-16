package cn.ninanina.wushanvideo.ui.me;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.network.CommonPresenter;

public class RegisterFragment extends Fragment {
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
                if (s.toString().length() >= 5 && s.toString().length() <= 16) {
                    CommonPresenter.getInstance().checkUsernameExists(s.toString(), RegisterFragment.this);
                } else {
                    usernameOK = false;
                    usernameOKIcon.setVisibility(View.INVISIBLE);
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
                if (s.toString().length() >= 5 && s.toString().length() <= 16) {
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

        registerButton.setOnClickListener(v -> CommonPresenter.getInstance().register(RegisterFragment.this));
    }

    private void showHideRegister() {
        if (usernameOK && passwordOK && genderOK) {
            registerButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.buttonClickColor, null));
            registerButton.setEnabled(true);
        } else {
            registerButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.transparent, null));
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
