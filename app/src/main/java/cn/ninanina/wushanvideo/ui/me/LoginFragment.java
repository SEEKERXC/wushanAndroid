package cn.ninanina.wushanvideo.ui.me;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ninanina.wushanvideo.R;
import cn.ninanina.wushanvideo.network.CommonPresenter;

public class LoginFragment extends Fragment {
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

        loginButton.setOnClickListener(v -> CommonPresenter.getInstance().login(LoginFragment.this));
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
