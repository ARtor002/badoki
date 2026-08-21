package ir.artor.badoki.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import ir.artor.badoki.AuthActivity;
import ir.artor.badoki.BadokiApp;
import ir.artor.badoki.R;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.util.Avatar;
import ir.artor.badoki.util.Fmt;
import ir.artor.badoki.util.SessionManager;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** پروفایل پزشک: نمایش اطلاعات و ویرایش ساعات کاری، هزینه و بیو */
public class DoctorProfileFragment extends Fragment {

    private Models.Doctor doctor;
    private View loadingView, errorView, contentView;
    private TextView avatar, name, specialty, price, location, address, bio, hours;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_doctor_profile, container, false);

        loadingView = root.findViewById(R.id.loading_view);
        errorView = root.findViewById(R.id.error_view);
        contentView = root.findViewById(R.id.content_root);
        root.findViewById(R.id.retry_btn).setOnClickListener(v -> load());

        avatar = root.findViewById(R.id.avatar);
        name = root.findViewById(R.id.name);
        specialty = root.findViewById(R.id.specialty);
        price = root.findViewById(R.id.price);
        location = root.findViewById(R.id.location);
        address = root.findViewById(R.id.address);
        bio = root.findViewById(R.id.bio);
        hours = root.findViewById(R.id.hours);

        root.findViewById(R.id.btn_edit).setOnClickListener(v -> showEditDialog());
        root.findViewById(R.id.btn_logout).setOnClickListener(v -> confirmLogout());

        load();
        return root;
    }

    /** خروج از حساب با دیالوگ تأیید */
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

    private void load() {
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        contentView.setVisibility(View.GONE);

        BadokiApp.api().doctorMe().enqueue(new Callback<Models.Doctor>() {
            @Override
            public void onResponse(@NonNull Call<Models.Doctor> call,
                                   @NonNull Response<Models.Doctor> response) {
                loadingView.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    doctor = response.body();
                    render(doctor);
                    contentView.setVisibility(View.VISIBLE);
                } else {
                    errorView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.Doctor> call, @NonNull Throwable t) {
                loadingView.setVisibility(View.GONE);
                if (ApiClient.isUnauthorized(t)) {
                    ApiClient.handleUnauthorized(requireContext());
                    return;
                }
                errorView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void render(Models.Doctor d) {
        avatar.setText(Avatar.initials(d.fullName));
        avatar.setBackgroundTintList(Avatar.tintFor(requireContext(), d.fullName));
        avatar.setTextColor(Avatar.textColor(requireContext()));
        name.setText(d.fullName);
        specialty.setText(d.specialty);
        price.setText(Fmt.toman(d.visitPrice));
        location.setText((d.hospitalName == null || d.hospitalName.isEmpty() ? "" : d.hospitalName + "، ")
                + d.city);
        address.setText(d.address);
        bio.setText(d.bio);
        hours.setText(Fmt.fa(d.startHour) + ":۰۰ تا " + Fmt.fa(d.endHour) + ":۰۰"
                + "  •  هر " + Fmt.fa(d.slotMinutes) + " دقیقه");
    }

    private void showEditDialog() {
        if (doctor == null) return;
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_doctor_profile_edit, null);

        TextInputEditText priceField = dialogView.findViewById(R.id.edit_price);
        TextInputEditText hospitalField = dialogView.findViewById(R.id.edit_hospital);
        TextInputEditText addressField = dialogView.findViewById(R.id.edit_address);
        TextInputEditText bioField = dialogView.findViewById(R.id.edit_bio);
        TextInputEditText startField = dialogView.findViewById(R.id.edit_start);
        TextInputEditText endField = dialogView.findViewById(R.id.edit_end);
        TextInputEditText slotField = dialogView.findViewById(R.id.edit_slot);
        ChipGroup daysGroup = dialogView.findViewById(R.id.edit_days);
        TextView errorText = dialogView.findViewById(R.id.edit_error);

        priceField.setText(String.valueOf(doctor.visitPrice));
        hospitalField.setText(doctor.hospitalName);
        addressField.setText(doctor.address);
        bioField.setText(doctor.bio);
        startField.setText(String.valueOf(doctor.startHour));
        endField.setText(String.valueOf(doctor.endHour));
        slotField.setText(String.valueOf(doctor.slotMinutes));

        Set<String> current = new HashSet<>();
        if (doctor.availableDays != null) current.addAll(doctor.availableDays);
        for (DayOfWeek day : DayOfWeek.values()) {
            Chip chip = new Chip(requireContext());
            chip.setText(dayNameFa(day));
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setTag(day.name());
            chip.setChecked(current.contains(day.name()));
            daysGroup.addView(chip);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.doctor_profile_edit)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (d, w) -> {
                    try {
                        long price = Long.parseLong(Fmt.en(textOf(priceField)));
                        int start = Integer.parseInt(Fmt.en(textOf(startField)));
                        int end = Integer.parseInt(Fmt.en(textOf(endField)));
                        int slot = Integer.parseInt(Fmt.en(textOf(slotField)));
                        List<String> days = new ArrayList<>();
                        for (int i = 0; i < daysGroup.getChildCount(); i++) {
                            Chip chip = (Chip) daysGroup.getChildAt(i);
                            if (chip.isChecked()) days.add((String) chip.getTag());
                        }
                        if (days.isEmpty() || start >= end) {
                            errorText.setVisibility(View.VISIBLE);
                            return;
                        }
                        saveProfile(price, textOf(hospitalField), textOf(addressField),
                                textOf(bioField), start, end, slot, days);
                    } catch (NumberFormatException e) {
                        errorText.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String textOf(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private void saveProfile(long price, String hospital, String address, String bio,
                             int start, int end, int slot, List<String> days) {
        Models.DoctorProfileUpdate request = new Models.DoctorProfileUpdate();
        request.visitPrice = price;
        request.hospitalName = hospital.isEmpty() ? null : hospital;
        request.address = address.isEmpty() ? null : address;
        request.bio = bio.isEmpty() ? null : bio;
        request.startHour = start;
        request.endHour = end;
        request.slotMinutes = slot;
        request.availableDays = days;

        // به‌روزرسانی خوش‌بینانه
        Models.Doctor optimistic = doctor;
        optimistic.visitPrice = price;
        optimistic.hospitalName = request.hospitalName;
        optimistic.address = request.address;
        optimistic.bio = request.bio;
        optimistic.startHour = start;
        optimistic.endHour = end;
        optimistic.slotMinutes = slot;
        optimistic.availableDays = days;
        render(optimistic);
        Toast.makeText(requireContext(), R.string.doctor_profile_saved, Toast.LENGTH_SHORT).show();

        BadokiApp.api().updateDoctorMe(request).enqueue(new Callback<Models.Doctor>() {
            @Override
            public void onResponse(@NonNull Call<Models.Doctor> call,
                                   @NonNull Response<Models.Doctor> response) {
                if (response.isSuccessful() && response.body() != null) {
                    doctor = response.body();
                    render(doctor);
                } else {
                    render(doctor);
                    Toast.makeText(requireContext(), R.string.action_failed_rollback,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.Doctor> call, @NonNull Throwable t) {
                render(doctor);
                Toast.makeText(requireContext(), R.string.action_failed_rollback,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String dayNameFa(DayOfWeek day) {
        int res;
        switch (day) {
            case SATURDAY: res = R.string.weekday_SATURDAY; break;
            case SUNDAY: res = R.string.weekday_SUNDAY; break;
            case MONDAY: res = R.string.weekday_MONDAY; break;
            case TUESDAY: res = R.string.weekday_TUESDAY; break;
            case WEDNESDAY: res = R.string.weekday_WEDNESDAY; break;
            case THURSDAY: res = R.string.weekday_THURSDAY; break;
            default: res = R.string.weekday_FRIDAY;
        }
        return getString(res);
    }
}
