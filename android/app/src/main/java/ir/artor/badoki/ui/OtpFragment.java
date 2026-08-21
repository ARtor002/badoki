package ir.artor.badoki.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import ir.artor.badoki.util.LoadingDialog;
import ir.artor.badoki.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * صفحه تأیید کد (مرحله دوم):
 * - حالت REGISTER: بعد از ارسال کد در ثبت‌نام
 * - حالت LOGIN: بعد از ورود با رمز (2FA)
 */
public class OtpFragment extends Fragment {

    public static final String MODE_REGISTER = "register";
    public static final String MODE_LOGIN = "login";

    private static final String ARG_MODE = "mode";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_DEV_OTP = "dev_otp";
    private static final String ARG_NAME = "name";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_PASSWORD = "password";

    public static OtpFragment forRegister(String email, String devOtp,
                                          String fullName, String phone, String password) {
        OtpFragment f = new OtpFragment();
        Bundle b = new Bundle();
        b.putString(ARG_MODE, MODE_REGISTER);
        b.putString(ARG_EMAIL, email);
        b.putString(ARG_DEV_OTP, devOtp);
        b.putString(ARG_NAME, fullName);
        b.putString(ARG_PHONE, phone);
        b.putString(ARG_PASSWORD, password);
        f.setArguments(b);
        return f;
    }

    public static OtpFragment forLogin(String email, String devOtp) {
        OtpFragment f = new OtpFragment();
        Bundle b = new Bundle();
        b.putString(ARG_MODE, MODE_LOGIN);
        b.putString(ARG_EMAIL, email);
        b.putString(ARG_DEV_OTP, devOtp);
        f.setArguments(b);
        return f;
    }

    private String mode, email, devOtp;
    private final LoadingDialog loadingDialog = new LoadingDialog();
    private TextInputEditText codeField;
    private Button verifyBtn;
    private TextView errorText, hintText, resendBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_otp, container, false);

        Bundle args = getArguments();
        mode = args.getString(ARG_MODE);
        email = args.getString(ARG_EMAIL);
        devOtp = args.getString(ARG_DEV_OTP);

        ((TextView) root.findViewById(R.id.otp_subtitle)).setText(
                getString(R.string.otp_sent_to) + " " + email);
        codeField = root.findViewById(R.id.otp_code);
        verifyBtn = root.findViewById(R.id.otp_verify);
        errorText = root.findViewById(R.id.otp_error);
        hintText = root.findViewById(R.id.otp_hint);
        resendBtn = root.findViewById(R.id.otp_resend);

        if (devOtp != null && !devOtp.isEmpty()) {
            hintText.setText(getString(R.string.otp_dev_hint) + " " + devOtp);
            hintText.setVisibility(View.VISIBLE);
        }

        codeField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                errorText.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        verifyBtn.setOnClickListener(v -> verify());
        resendBtn.setOnClickListener(v -> resend());
        return root;
    }

    private void verify() {
        String code = codeField.getText() == null ? "" : codeField.getText().toString().trim();
        if (code.length() != 6) {
            errorText.setText(R.string.otp_invalid);
            errorText.setVisibility(View.VISIBLE);
            return;
        }
        setLoading(true);
        Call<Models.AuthResponse> call;
        if (MODE_REGISTER.equals(mode)) {
            Models.VerifyRegisterRequest req = new Models.VerifyRegisterRequest();
            req.email = email;
            req.otp = code;
            req.fullName = getArguments().getString(ARG_NAME);
            req.phone = getArguments().getString(ARG_PHONE);
            req.password = getArguments().getString(ARG_PASSWORD);
            call = BadokiApp.api().verifyRegister(req);
        } else {
            Models.VerifyLoginRequest req = new Models.VerifyLoginRequest();
            req.email = email;
            req.otp = code;
            call = BadokiApp.api().verifyLogin(req);
        }
        call.enqueue(new Callback<Models.AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<Models.AuthResponse> call,
                                   @NonNull Response<Models.AuthResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().token != null) {
                    SessionManager.saveLogin(response.body());
                    ((AuthActivity) requireActivity()).openMain();
                } else {
                    errorText.setText(ApiClient.errorMessage(new retrofit2.HttpException(response)));
                    errorText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.AuthResponse> call, @NonNull Throwable t) {
                setLoading(false);
                errorText.setText(ApiClient.errorMessage(t));
                errorText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void resend() {
        resendBtn.setEnabled(false);
        loadingDialog.show(requireContext(), getString(R.string.loading_sending_otp));
        // فقط حالت REGISTER امکان ارسال مجدد دارد؛ در LOGIN باید دوباره لاگین کرد
        if (!MODE_REGISTER.equals(mode)) {
            resendBtn.setEnabled(true);
            errorText.setText(R.string.otp_login_resend);
            errorText.setVisibility(View.VISIBLE);
            return;
        }
        Models.EmailRequest req = new Models.EmailRequest();
        req.email = email;
        BadokiApp.api().sendRegisterOtp(req).enqueue(new Callback<Models.OtpResponse>() {
            @Override
            public void onResponse(@NonNull Call<Models.OtpResponse> c,
                                   @NonNull Response<Models.OtpResponse> response) {
                resendBtn.setEnabled(true);
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().devOtp != null && !response.body().devOtp.isEmpty()) {
                        hintText.setText(getString(R.string.otp_dev_hint) + " " + response.body().devOtp);
                        hintText.setVisibility(View.VISIBLE);
                    }
                    errorText.setText(R.string.otp_resent);
                    errorText.setVisibility(View.VISIBLE);
                } else {
                    errorText.setText(ApiClient.errorMessage(new retrofit2.HttpException(response)));
                    errorText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.OtpResponse> c, @NonNull Throwable t) {
                resendBtn.setEnabled(true);
                loadingDialog.dismiss();
                errorText.setText(ApiClient.errorMessage(t));
                errorText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setLoading(boolean loading) {
        verifyBtn.setEnabled(!loading);
        verifyBtn.setText(loading ? R.string.loading : R.string.otp_verify);
    }
}
