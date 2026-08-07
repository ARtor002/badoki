package ir.artor.badoki.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ir.artor.badoki.AuthActivity;
import ir.artor.badoki.BadokiApp;
import ir.artor.badoki.R;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.util.Avatar;
import ir.artor.badoki.util.Fmt;
import ir.artor.badoki.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** پروفایل کاربر: نمایش اطلاعات، ویرایش و خروج */
public class ProfileFragment extends Fragment {

    private TextView avatar, name, email, phone, memberSince;
    private Button logoutBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        avatar = root.findViewById(R.id.avatar);
        name = root.findViewById(R.id.profile_name);
        email = root.findViewById(R.id.profile_email);
        phone = root.findViewById(R.id.profile_phone);
        memberSince = root.findViewById(R.id.member_since);
        logoutBtn = root.findViewById(R.id.btn_logout);

        root.findViewById(R.id.btn_edit).setOnClickListener(v -> showEditDialog());
        logoutBtn.setOnClickListener(v -> confirmLogout());

        loadProfile();
        return root;
    }

    private void loadProfile() {
        BadokiApp.api().me().enqueue(new Callback<Models.User>() {
            @Override
            public void onResponse(@NonNull Call<Models.User> call,
                                   @NonNull Response<Models.User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    render(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.User> call, @NonNull Throwable t) {
                if (ApiClient.isUnauthorized(t)) {
                    ApiClient.handleUnauthorized(requireContext());
                }
            }
        });
    }

    private void render(Models.User user) {
        avatar.setText(Avatar.initials(user.fullName));
        avatar.setBackgroundTintList(Avatar.tintFor(requireContext(), user.fullName));
        avatar.setTextColor(Avatar.textColor(requireContext()));
        name.setText(user.fullName);
        email.setText(user.email);
        phone.setText(user.phone == null || user.phone.isEmpty()
                ? "—" : Fmt.fa(user.phone));

        // تاریخ عضویت از createdAt
        if (user.phone != null) SessionManager.updateProfile(user.fullName, user.phone);
    }

    private void showEditDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_profile, null);
        TextInputEditText nameField = dialogView.findViewById(R.id.edit_name);
        TextInputEditText phoneField = dialogView.findViewById(R.id.edit_phone);
        nameField.setText(SessionManager.name());
        phoneField.setText(SessionManager.phone());

        TextView errorText = dialogView.findViewById(R.id.edit_error);
        TextWatcher clearError = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                errorText.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        nameField.addTextChangedListener(clearError);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_edit_title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String newName = nameField.getText() == null ? "" : nameField.getText().toString().trim();
                    String newPhone = phoneField.getText() == null ? "" : phoneField.getText().toString().trim();
                    if (newName.isEmpty()) {
                        errorText.setVisibility(View.VISIBLE);
                        return;
                    }
                    saveProfileOptimistic(newName, newPhone);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /** ذخیره پروفایل با به‌روزرسانی خوش‌بینانه */
    private void saveProfileOptimistic(String newName, String newPhone) {
        String oldName = SessionManager.name();
        String oldPhone = SessionManager.phone();

        // اعمال فوری در UI
        SessionManager.updateProfile(newName, newPhone);
        name.setText(newName);
        phone.setText(newPhone.isEmpty() ? "—" : Fmt.fa(newPhone));
        avatar.setText(Avatar.initials(newName));
        avatar.setBackgroundTintList(Avatar.tintFor(requireContext(), newName));
        if (getActivity() instanceof ir.artor.badoki.MainActivity) {
            ((ir.artor.badoki.MainActivity) getActivity()).updateDrawerHeader();
        }

        Models.ProfileUpdateRequest request = new Models.ProfileUpdateRequest();
        request.fullName = newName;
        request.phone = newPhone.isEmpty() ? null : newPhone;

        BadokiApp.api().updateMe(request).enqueue(new Callback<Models.User>() {
            @Override
            public void onResponse(@NonNull Call<Models.User> call,
                                   @NonNull Response<Models.User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
                } else {
                    revertProfile(oldName, oldPhone);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.User> call, @NonNull Throwable t) {
                revertProfile(oldName, oldPhone);
            }
        });
    }

    private void revertProfile(String name, String phone) {
        SessionManager.updateProfile(name, phone);
        this.name.setText(name);
        this.phone.setText(phone.isEmpty() ? "—" : Fmt.fa(phone));
        this.avatar.setText(Avatar.initials(name));
        this.avatar.setBackgroundTintList(Avatar.tintFor(requireContext(), name));
        Toast.makeText(requireContext(), R.string.action_failed_rollback, Toast.LENGTH_SHORT).show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.confirm, (d, w) -> {
                    SessionManager.logout();
                    Intent intent = new Intent(requireContext(), AuthActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
