package ir.artor.badoki.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ir.artor.badoki.AuthActivity;
import ir.artor.badoki.BadokiApp;
import ir.artor.badoki.R;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private TextInputLayout emailInput;
    private TextInputLayout passwordInput;
    private TextInputEditText emailField;
    private TextInputEditText passwordField;
    private TextView errorText;
    private Button loginBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        emailInput = root.findViewById(R.id.email_input);
        passwordInput = root.findViewById(R.id.password_input);
        emailField = root.findViewById(R.id.email_field);
        passwordField = root.findViewById(R.id.password_field);
        errorText = root.findViewById(R.id.login_error);
        loginBtn = root.findViewById(R.id.login_btn);

        root.findViewById(R.id.login_go_register).setOnClickListener(v ->
                ((AuthActivity) requireActivity()).showRegister());
        root.findViewById(R.id.login_forgot).setOnClickListener(v ->
                ((AuthActivity) requireActivity()).showForgotPassword());

        // پاک کردن خطا هنگام تایپ
        TextWatcher clearError = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                if (errorText.getVisibility() == View.VISIBLE) errorText.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        emailField.addTextChangedListener(clearError);
        passwordField.addTextChangedListener(clearError);

        loginBtn.setOnClickListener(v -> attemptLogin());
        return root;
    }

    private void attemptLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().toString().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().toString();

        boolean valid = true;
        if (TextUtils.isEmpty(email)) {
            emailInput.setError(getString(R.string.err_email_required));
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError(getString(R.string.err_email_invalid));
            valid = false;
        } else {
            emailInput.setError(null);
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError(getString(R.string.err_password_required));
            valid = false;
        } else {
            passwordInput.setError(null);
        }
        if (!valid) return;

        setLoading(true);
        Models.LoginRequest request = new Models.LoginRequest();
        request.email = email;
        request.password = password;

        BadokiApp.api().login(request).enqueue(new Callback<Models.AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<Models.AuthResponse> call,
                                   @NonNull Response<Models.AuthResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Models.AuthResponse body = response.body();
                    if (body.requiresOtp) {
                        // مرحله دوم: کد تأیید
                        ((AuthActivity) requireActivity()).openOtp(
                                OtpFragment.forLogin(body.email, body.devOtp));
                    } else {
                        SessionManager.saveLogin(body);
                        ((AuthActivity) requireActivity()).openMain();
                    }
                } else {
                    showError(ApiClient.errorMessage(
                            new retrofit2.HttpException(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.AuthResponse> call, @NonNull Throwable t) {
                setLoading(false);
                showError(ApiClient.errorMessage(t));
            }
        });
    }

    private void setLoading(boolean loading) {
        loginBtn.setEnabled(!loading);
        loginBtn.setText(loading ? R.string.loading : R.string.login_btn);
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
