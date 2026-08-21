package ir.artor.badoki.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import ir.artor.badoki.AuthActivity;
import ir.artor.badoki.BadokiApp;
import ir.artor.badoki.R;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** فراموشی رمز عبور: ایمیل ← کد ← رمز جدید */
public class ForgotPasswordFragment extends Fragment {

    private TextInputEditText emailField, codeField, passwordField;
    private Button sendBtn, resetBtn;
    private TextView errorText, hintText, stepHint;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        emailField = root.findViewById(R.id.fp_email);
        codeField = root.findViewById(R.id.fp_code);
        passwordField = root.findViewById(R.id.fp_password);
        sendBtn = root.findViewById(R.id.fp_send);
        resetBtn = root.findViewById(R.id.fp_reset);
        errorText = root.findViewById(R.id.fp_error);
        hintText = root.findViewById(R.id.fp_hint);
        stepHint = root.findViewById(R.id.fp_step_hint);

        root.findViewById(R.id.fp_back_login).setOnClickListener(v ->
                ((AuthActivity) requireActivity()).showLogin());

        sendBtn.setOnClickListener(v -> {
            String email = textOf(emailField);
            if (email.isEmpty()) {
                errorText.setText(R.string.err_email_required);
                errorText.setVisibility(View.VISIBLE);
                return;
            }
            sendBtn.setEnabled(false);
            sendBtn.setText(R.string.loading);
            Models.ForgotPasswordRequest req = new Models.ForgotPasswordRequest();
            req.email = email;
            BadokiApp.api().forgotPassword(req).enqueue(new Callback<Models.OtpResponse>() {
                @Override
                public void onResponse(@NonNull Call<Models.OtpResponse> c,
                                       @NonNull Response<Models.OtpResponse> response) {
                    sendBtn.setEnabled(true);
                    sendBtn.setText(R.string.forgot_send);
                    if (response.isSuccessful() && response.body() != null) {
                        codeField.setVisibility(View.VISIBLE);
                        passwordField.setVisibility(View.VISIBLE);
                        resetBtn.setVisibility(View.VISIBLE);
                        stepHint.setVisibility(View.VISIBLE);
                        if (response.body().devOtp != null && !response.body().devOtp.isEmpty()) {
                            hintText.setText(getString(R.string.otp_dev_hint) + " " + response.body().devOtp);
                            hintText.setVisibility(View.VISIBLE);
                        }
                        errorText.setVisibility(View.GONE);
                    } else {
                        errorText.setText(ApiClient.errorMessage(new retrofit2.HttpException(response)));
                        errorText.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Models.OtpResponse> c, @NonNull Throwable t) {
                    sendBtn.setEnabled(true);
                    sendBtn.setText(R.string.forgot_send);
                    errorText.setText(ApiClient.errorMessage(t));
                    errorText.setVisibility(View.VISIBLE);
                }
            });
        });

        resetBtn.setOnClickListener(v -> {
            String email = textOf(emailField);
            String code = textOf(codeField);
            String pass = textOf(passwordField);
            if (code.length() != 6) {
                errorText.setText(R.string.otp_invalid);
                errorText.setVisibility(View.VISIBLE);
                return;
            }
            if (pass.length() < 6) {
                errorText.setText(R.string.err_password_short);
                errorText.setVisibility(View.VISIBLE);
                return;
            }
            resetBtn.setEnabled(false);
            Models.ResetPasswordRequest req = new Models.ResetPasswordRequest();
            req.email = email;
            req.otp = code;
            req.newPassword = pass;
            BadokiApp.api().resetPassword(req).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> c, @NonNull Response<Void> response) {
                    resetBtn.setEnabled(true);
                    if (response.isSuccessful()) {
                        new android.app.AlertDialog.Builder(requireContext())
                                .setTitle(R.string.forgot_success_title)
                                .setMessage(R.string.forgot_success_msg)
                                .setCancelable(false)
                                .setPositiveButton(R.string.confirm, (d, w) ->
                                        ((AuthActivity) requireActivity()).showLogin())
                                .show();
                    } else {
                        errorText.setText(ApiClient.errorMessage(new retrofit2.HttpException(response)));
                        errorText.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> c, @NonNull Throwable t) {
                    resetBtn.setEnabled(true);
                    errorText.setText(ApiClient.errorMessage(t));
                    errorText.setVisibility(View.VISIBLE);
                }
            });
        });
        return root;
    }

    private String textOf(TextInputEditText f) {
        return f.getText() == null ? "" : f.getText().toString().trim();
    }
}
