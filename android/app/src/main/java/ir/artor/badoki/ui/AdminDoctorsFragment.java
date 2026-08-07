package ir.artor.badoki.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import ir.artor.badoki.BadokiApp;
import ir.artor.badoki.R;
import ir.artor.badoki.adapter.AdminDoctorAdapter;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.util.Fmt;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** CRUD کامل پزشکان برای ادمین */
public class AdminDoctorsFragment extends Fragment {

    private RecyclerView rv;
    private View loadingView, errorView, emptyView;
    private AdminDoctorAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_doctors, container, false);

        rv = root.findViewById(R.id.doctors_rv);
        loadingView = root.findViewById(R.id.loading_view);
        errorView = root.findViewById(R.id.error_view);
        emptyView = root.findViewById(R.id.empty_view);
        root.findViewById(R.id.retry_btn).setOnClickListener(v -> load());

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminDoctorAdapter(new AdminDoctorAdapter.Listener() {
            @Override public void onEdit(Models.Doctor d) { showForm(d); }
            @Override public void onDelete(Models.Doctor d) { confirmDelete(d); }
        });
        rv.setAdapter(adapter);

        FloatingActionButton fab = root.findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> showForm(null));

        load();
        return root;
    }

    private void load() {
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        BadokiApp.api().adminDoctors().enqueue(new Callback<List<Models.Doctor>>() {
            @Override
            public void onResponse(@NonNull Call<List<Models.Doctor>> call,
                                   @NonNull Response<List<Models.Doctor>> response) {
                loadingView.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.submit(response.body());
                    boolean empty = response.body().isEmpty();
                    emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
                    rv.setVisibility(empty ? View.GONE : View.VISIBLE);
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Models.Doctor>> call, @NonNull Throwable t) {
                loadingView.setVisibility(View.GONE);
                if (ApiClient.isUnauthorized(t)) {
                    ApiClient.handleUnauthorized(requireContext());
                    return;
                }
                showError();
            }
        });
    }

    private void showError() {
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        rv.setVisibility(View.GONE);
    }

    private void confirmDelete(Models.Doctor doctor) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.admin_delete_confirm_title)
                .setMessage(R.string.admin_delete_confirm_message)
                .setPositiveButton(R.string.confirm, (d, w) -> deleteOptimistic(doctor))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteOptimistic(Models.Doctor doctor) {
        adapter.removeById(doctor.id);
        if (adapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        }
        Toast.makeText(requireContext(), R.string.admin_doctor_deleted, Toast.LENGTH_SHORT).show();

        BadokiApp.api().adminDeleteDoctor(doctor.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!response.isSuccessful()) {
                    load();
                    Snackbar.make(getView(), R.string.action_failed_rollback, Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                load();
                Snackbar.make(getView(), R.string.action_failed_rollback, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    // ---------- فرم ساخت/ویرایش ----------

    private void showForm(@Nullable Models.Doctor existing) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_doctor_form, null);

        TextInputEditText nameField = dialogView.findViewById(R.id.form_name);
        TextInputEditText specialtyField = dialogView.findViewById(R.id.form_specialty);
        TextInputEditText cityField = dialogView.findViewById(R.id.form_city);
        TextInputEditText hospitalField = dialogView.findViewById(R.id.form_hospital);
        TextInputEditText addressField = dialogView.findViewById(R.id.form_address);
        TextInputEditText bioField = dialogView.findViewById(R.id.form_bio);
        TextInputEditText priceField = dialogView.findViewById(R.id.form_price);
        TextInputEditText expField = dialogView.findViewById(R.id.form_experience);
        TextInputEditText startField = dialogView.findViewById(R.id.form_start);
        TextInputEditText endField = dialogView.findViewById(R.id.form_end);
        TextInputEditText slotField = dialogView.findViewById(R.id.form_slot);
        TextInputEditText emailField = dialogView.findViewById(R.id.form_email);
        TextInputEditText passwordField = dialogView.findViewById(R.id.form_password);
        ChipGroup daysGroup = dialogView.findViewById(R.id.form_days);
        View accountSection = dialogView.findViewById(R.id.account_section);
        TextView errorText = dialogView.findViewById(R.id.form_error);

        // بخش حساب فقط هنگام ساخت نمایش داده می‌شود
        if (existing != null) {
            accountSection.setVisibility(View.GONE);
            nameField.setText(existing.fullName);
            specialtyField.setText(existing.specialty);
            cityField.setText(existing.city);
            hospitalField.setText(existing.hospitalName);
            addressField.setText(existing.address);
            bioField.setText(existing.bio);
            priceField.setText(String.valueOf(existing.visitPrice));
            expField.setText(String.valueOf(existing.experienceYears));
            startField.setText(String.valueOf(existing.startHour));
            endField.setText(String.valueOf(existing.endHour));
            slotField.setText(String.valueOf(existing.slotMinutes));
        }

        Set<String> currentDays = new HashSet<>();
        if (existing != null && existing.availableDays != null) {
            currentDays.addAll(existing.availableDays);
        }
        for (DayOfWeek day : DayOfWeek.values()) {
            Chip chip = new Chip(requireContext());
            chip.setText(dayNameFa(day));
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setTag(day.name());
            chip.setChecked(currentDays.contains(day.name()));
            daysGroup.addView(chip);
        }

        boolean isEdit = existing != null;
        new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? R.string.admin_edit_doctor : R.string.admin_add_doctor)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (d, w) -> {
                    try {
                        String name = textOf(nameField);
                        String specialty = textOf(specialtyField);
                        String city = textOf(cityField);
                        if (name.isEmpty() || specialty.isEmpty() || city.isEmpty()) {
                            errorText.setVisibility(View.VISIBLE);
                            return;
                        }
                        List<String> days = new ArrayList<>();
                        for (int i = 0; i < daysGroup.getChildCount(); i++) {
                            Chip chip = (Chip) daysGroup.getChildAt(i);
                            if (chip.isChecked()) days.add((String) chip.getTag());
                        }
                        long price = Long.parseLong(Fmt.en(textOf(priceField)));
                        int exp = textOf(expField).isEmpty() ? 0 : Integer.parseInt(Fmt.en(textOf(expField)));
                        int start = Integer.parseInt(Fmt.en(textOf(startField)));
                        int end = Integer.parseInt(Fmt.en(textOf(endField)));
                        int slot = Integer.parseInt(Fmt.en(textOf(slotField)));
                        if (days.isEmpty() || start >= end) {
                            errorText.setVisibility(View.VISIBLE);
                            return;
                        }

                        Models.AdminDoctorRequest request = new Models.AdminDoctorRequest();
                        request.fullName = name;
                        request.specialty = specialty;
                        request.city = city;
                        request.hospitalName = textOf(hospitalField);
                        request.address = textOf(addressField);
                        request.bio = textOf(bioField);
                        request.visitPrice = price;
                        request.experienceYears = exp;
                        request.startHour = start;
                        request.endHour = end;
                        request.slotMinutes = slot;
                        request.availableDays = days;
                        if (!isEdit) {
                            request.email = textOf(emailField);
                            request.password = textOf(passwordField);
                        }
                        saveDoctor(existing, request);
                    } catch (NumberFormatException e) {
                        errorText.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void saveDoctor(@Nullable Models.Doctor existing, Models.AdminDoctorRequest request) {
        if (existing == null) {
            BadokiApp.api().adminCreateDoctor(request).enqueue(new Callback<Models.Doctor>() {
                @Override
                public void onResponse(@NonNull Call<Models.Doctor> call,
                                       @NonNull Response<Models.Doctor> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.replace(response.body());
                        emptyView.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(), R.string.admin_doctor_saved,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(getView(),
                                ApiClient.errorMessage(new retrofit2.HttpException(response)),
                                Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Models.Doctor> call, @NonNull Throwable t) {
                    Snackbar.make(getView(), ApiClient.errorMessage(t), Snackbar.LENGTH_LONG).show();
                }
            });
        } else {
            BadokiApp.api().adminUpdateDoctor(existing.id, request).enqueue(new Callback<Models.Doctor>() {
                @Override
                public void onResponse(@NonNull Call<Models.Doctor> call,
                                       @NonNull Response<Models.Doctor> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.replace(response.body());
                        Toast.makeText(requireContext(), R.string.admin_doctor_saved,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(getView(),
                                ApiClient.errorMessage(new retrofit2.HttpException(response)),
                                Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Models.Doctor> call, @NonNull Throwable t) {
                    Snackbar.make(getView(), ApiClient.errorMessage(t), Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    private String textOf(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
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
