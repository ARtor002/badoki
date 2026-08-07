package ir.artor.badoki.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import ir.artor.badoki.BadokiApp;
import ir.artor.badoki.MainActivity;
import ir.artor.badoki.R;
import ir.artor.badoki.adapter.AppointmentAdapter;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** لیست نوبت‌ها با فیلتر، لغو، تغییر زمان و حذف — به‌روزرسانی خوش‌بینانه */
public class AppointmentsFragment extends Fragment {

    private ChipGroup filterChips;
    private SwipeRefreshLayout swipe;
    private RecyclerView rv;
    private View emptyView, loadingView, errorView;
    private AppointmentAdapter adapter;

    private String filter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_appointments, container, false);

        filterChips = root.findViewById(R.id.filter_chips);
        swipe = root.findViewById(R.id.swipe);
        rv = root.findViewById(R.id.appt_rv);
        emptyView = root.findViewById(R.id.empty_view);
        loadingView = root.findViewById(R.id.loading_view);
        errorView = root.findViewById(R.id.error_view);
        root.findViewById(R.id.retry_btn).setOnClickListener(v -> load());
        root.findViewById(R.id.empty_cta).setOnClickListener(v -> openDoctors());

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AppointmentAdapter(new AppointmentAdapter.Listener() {
            @Override public void onCancel(Models.Appointment a) { confirmCancel(a); }
            @Override public void onReschedule(Models.Appointment a) { openReschedule(a); }
            @Override public void onDelete(Models.Appointment a) { confirmDelete(a); }
        });
        rv.setAdapter(adapter);

        swipe.setColorSchemeResources(R.color.primary);
        swipe.setOnRefreshListener(this::load);

        buildFilterChips();
        load();
        return root;
    }

    private void buildFilterChips() {
        filterChips.setSingleSelection(true);
        String[] labels = {getString(R.string.tab_all), getString(R.string.tab_upcoming),
                getString(R.string.tab_past), getString(R.string.tab_canceled)};
        String[] values = {"all", "upcoming", "past", "canceled"};
        for (int i = 0; i < labels.length; i++) {
            Chip chip = new Chip(requireContext());
            chip.setText(labels[i]);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setTag(values[i]);
            if (i == 0) chip.setChecked(true);
            filterChips.addView(chip);
        }
        filterChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = checkedIds.isEmpty() ? -1 : checkedIds.get(0);
            Chip checked = id == -1 ? null : group.findViewById(id);
            if (checked == null || checked.getTag() == null) return;
            filter = (String) checked.getTag();
            load();
        });
    }

    private void load() {
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        BadokiApp.api().appointments(filter).enqueue(new Callback<List<Models.Appointment>>() {
            @Override
            public void onResponse(@NonNull Call<List<Models.Appointment>> call,
                                   @NonNull Response<List<Models.Appointment>> response) {
                swipe.setRefreshing(false);
                loadingView.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.submit(response.body());
                    updateEmptyState();
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Models.Appointment>> call, @NonNull Throwable t) {
                swipe.setRefreshing(false);
                loadingView.setVisibility(View.GONE);
                if (ApiClient.isUnauthorized(t)) {
                    ApiClient.handleUnauthorized(requireContext());
                    return;
                }
                showError();
            }
        });
    }

    private void updateEmptyState() {
        boolean empty = adapter.getItemCount() == 0;
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        rv.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showError() {
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        rv.setVisibility(View.GONE);
    }

    // ---------- لغو (خوش‌بینانه) ----------

    private void confirmCancel(Models.Appointment appointment) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.cancel_confirm_title)
                .setMessage(R.string.cancel_confirm_message)
                .setPositiveButton(R.string.confirm, (d, w) -> cancelOptimistic(appointment))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void cancelOptimistic(Models.Appointment appointment) {
        Models.Appointment optimistic = cloneAppointment(appointment);
        optimistic.status = "CANCELED";

        adapter.replace(optimistic);
        showSnackbar(getString(R.string.appointment_canceled_toast));

        BadokiApp.api().cancelAppointment(appointment.id)
                .enqueue(new Callback<Models.Appointment>() {
                    @Override
                    public void onResponse(@NonNull Call<Models.Appointment> call,
                                           @NonNull Response<Models.Appointment> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.replace(response.body());
                            updateEmptyState();
                        } else {
                            rollback(appointment, "cancel");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Models.Appointment> call, @NonNull Throwable t) {
                        rollback(appointment, "cancel");
                    }
                });
    }

    // ---------- تغییر زمان (خوش‌بینانه) ----------

    private void openReschedule(Models.Appointment appointment) {
        RescheduleSheet.newInstance(appointment).show(getChildFragmentManager(), "reschedule");
    }

    /** فراخوانی از RescheduleSheet بعد از انتخاب زمان جدید */
    public void onRescheduleChosen(Models.Appointment original, String date, String time) {
        Models.Appointment optimistic = cloneAppointment(original);
        optimistic.date = date;
        optimistic.time = time;
        optimistic.status = "CONFIRMED";

        adapter.replace(optimistic);
        showSnackbar(getString(R.string.appointment_rescheduled_toast));

        Models.RescheduleRequest request = new Models.RescheduleRequest();
        request.date = date;
        request.time = time;
        BadokiApp.api().reschedule(original.id, request)
                .enqueue(new Callback<Models.Appointment>() {
                    @Override
                    public void onResponse(@NonNull Call<Models.Appointment> call,
                                           @NonNull Response<Models.Appointment> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.replace(response.body());
                            updateEmptyState();
                        } else {
                            adapter.replace(original);
                            showSnackbar(getString(R.string.action_failed_rollback));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Models.Appointment> call, @NonNull Throwable t) {
                        adapter.replace(original);
                        showSnackbar(getString(R.string.action_failed_rollback));
                    }
                });
    }

    // ---------- حذف (خوش‌بینانه) ----------

    private void confirmDelete(Models.Appointment appointment) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setPositiveButton(R.string.confirm, (d, w) -> deleteOptimistic(appointment))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteOptimistic(Models.Appointment appointment) {
        int index = adapter.removeById(appointment.id);
        showSnackbar(getString(R.string.appointment_deleted_toast));
        updateEmptyState();

        BadokiApp.api().deleteAppointment(appointment.id)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (!response.isSuccessful()) {
                            adapter.restoreAt(index, appointment);
                            showSnackbar(getString(R.string.action_failed_rollback));
                            updateEmptyState();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        adapter.restoreAt(index, appointment);
                        showSnackbar(getString(R.string.action_failed_rollback));
                        updateEmptyState();
                    }
                });
    }

    private void rollback(Models.Appointment original, String action) {
        adapter.replace(original);
        showSnackbar(getString(R.string.action_failed_rollback));
        updateEmptyState();
    }

    private Models.Appointment cloneAppointment(Models.Appointment a) {
        Models.Appointment c = new Models.Appointment();
        c.id = a.id;
        c.doctorId = a.doctorId;
        c.doctorName = a.doctorName;
        c.specialty = a.specialty;
        c.city = a.city;
        c.hospitalName = a.hospitalName;
        c.visitPrice = a.visitPrice;
        c.date = a.date;
        c.time = a.time;
        c.status = a.status;
        c.notes = a.notes;
        return c;
    }

    private void showSnackbar(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void openDoctors() {
        ((MainActivity) requireActivity()).open(
                new DoctorsFragment(), getString(R.string.nav_doctors), false);
    }
}
