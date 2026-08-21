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

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private static final Pattern PHONE = Pattern.compile("^09\\d{9}$");

    private TextInputLayout nameInput, emailInput, phoneInput, passInput, pass2Input;
    private TextInputEditText nameField, emailField, phoneField, passField, pass2Field;
    private TextView errorText;
    private Button registerBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_register, container, false);

        nameInput = root.findViewById(R.id.name_input);
        emailInput = root.findViewById(R.id.email_input);
        phoneInput = root.findViewById(R.id.phone_input);
        passInput = root.findViewById(R.id.password_input);
        pass2Input = root.findViewById(R.id.password2_input);
        nameField = root.findViewById(R.id.name_field);
        emailField = root.findViewById(R.id.email_field);
        phoneField = root.findViewById(R.id.phone_field);
        passField = root.findViewById(R.id.password_field);
        pass2Field = root.findViewById(R.id.password2_field);
        errorText = root.findViewById(R.id.register_error);
        registerBtn = root.findViewById(R.id.register_btn);

        root.findViewById(R.id.register_go_login).setOnClickListener(v ->
                ((AuthActivity) requireActivity()).showLogin());

        TextWatcher clearError = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                if (errorText.getVisibility() == View.VISIBLE) errorText.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        nameField.addTextChangedListener(clearError);
        emailField.addTextChangedListener(clearError);
        phoneField.addTextChangedListener(clearError);
        passField.addTextChangedListener(clearError);
        pass2Field.addTextChangedListener(clearError);

        registerBtn.setOnClickListener(v -> attemptRegister());
        return root;
    }

    private void attemptRegister() {
        String name = textOf(nameField);
        String email = textOf(emailField).trim();
        String phone = textOf(phoneField).trim();
        String pass = textOf(passField);
        String pass2 = textOf(pass2Field);

        boolean valid = true;
        if (TextUtils.isEmpty(name)) { nameInput.setError(getString(R.string.err_name_required)); valid = false; }
        else nameInput.setError(null);

        if (TextUtils.isEmpty(email)) { emailInput.setError(getString(R.string.err_email_required)); valid = false; }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailInput.setError(getString(R.string.err_email_invalid)); valid = false; }
        else emailInput.setError(null);

        if (TextUtils.isEmpty(phone) || !PHONE.matcher(phone).matches()) { phoneInput.setError(getString(R.string.err_phone_invalid)); valid = false; }
        else phoneInput.setError(null);

        if (TextUtils.isEmpty(pass)) { passInput.setError(getString(R.string.err_password_required)); valid = false; }
        else if (pass.length() < 6) { passInput.setError(getString(R.string.err_password_short)); valid = false; }
        else passInput.setError(null);

        if (!pass.equals(pass2)) { pass2Input.setError(getString(R.string.err_password_mismatch)); valid = false; }
        else pass2Input.setError(null);

        if (!valid) return;

        setLoading(true);
        // گام ۱: ارسال کد تأیید به ایمیل
        Models.EmailRequest req = new Models.EmailRequest();
        req.email = email;
        BadokiApp.api().sendRegisterOtp(req).enqueue(new Callback<Models.OtpResponse>() {
            @Override
            public void onResponse(@NonNull Call<Models.OtpResponse> call,
                                   @NonNull Response<Models.OtpResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    // گام ۲: صفحه کد تأیید
                    ((AuthActivity) requireActivity()).openOtp(
                            OtpFragment.forRegister(email, response.body().devOtp,
                                    name, phone, pass));
                } else {
                    showError(ApiClient.errorMessage(
                            new retrofit2.HttpException(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.OtpResponse> call, @NonNull Throwable t) {
                setLoading(false);
                showError(ApiClient.errorMessage(t));
            }
        });
    }

    private String textOf(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString();
    }

    private void setLoading(boolean loading) {
        registerBtn.setEnabled(!loading);
        registerBtn.setText(loading ? R.string.loading : R.string.register_btn);
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
