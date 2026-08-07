package ir.artor.badoki.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import ir.artor.badoki.BadokiApp;
import ir.artor.badoki.R;
import ir.artor.badoki.adapter.DoctorAppointmentAdapter;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** نوبت‌های بیماران برای پزشک — تأیید، انجام‌شده و لغو با به‌روزرسانی خوش‌بینانه */
public class DoctorAppointmentsFragment extends Fragment {

    private ChipGroup filterChips;
    private SwipeRefreshLayout swipe;
    private RecyclerView rv;
    private View emptyView, loadingView, errorView;
    private DoctorAppointmentAdapter adapter;
    private String filter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_doctor_appointments, container, false);

        filterChips = root.findViewById(R.id.filter_chips);
        swipe = root.findViewById(R.id.swipe);
        rv = root.findViewById(R.id.appt_rv);
        emptyView = root.findViewById(R.id.empty_view);
        loadingView = root.findViewById(R.id.loading_view);
        errorView = root.findViewById(R.id.error_view);
        root.findViewById(R.id.retry_btn).setOnClickListener(v -> load());

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DoctorAppointmentAdapter(new DoctorAppointmentAdapter.Listener() {
            @Override public void onConfirm(Models.Appointment a) { changeStatus(a, "CONFIRMED"); }
            @Override public void onComplete(Models.Appointment a) { changeStatus(a, "COMPLETED"); }
            @Override public void onCancel(Models.Appointment a) { changeStatus(a, "CANCELED"); }
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
                getString(R.string.tab_past)};
        String[] values = {"all", "upcoming", "past"};
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
        BadokiApp.api().doctorAppointments(filter).enqueue(new Callback<List<Models.Appointment>>() {
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

    /** تغییر وضعیت نوبت با به‌روزرسانی خوش‌بینانه و بازگردانی در خطا */
    private void changeStatus(Models.Appointment appointment, String newStatus) {
        Models.Appointment optimistic = cloneAppointment(appointment);
        optimistic.status = newStatus;
        adapter.replace(optimistic);

        int toastRes = "CONFIRMED".equals(newStatus) ? R.string.appointment_confirmed_toast
                : "COMPLETED".equals(newStatus) ? R.string.appointment_completed_toast
                : R.string.appointment_canceled_toast;
        Snackbar.make(getView(), toastRes, Snackbar.LENGTH_SHORT).show();

        BadokiApp.api().updateAppointmentStatus(appointment.id,
                        new Models.UpdateStatusRequest(newStatus))
                .enqueue(new Callback<Models.Appointment>() {
                    @Override
                    public void onResponse(@NonNull Call<Models.Appointment> call,
                                           @NonNull Response<Models.Appointment> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.replace(response.body());
                        } else {
                            rollback(appointment);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Models.Appointment> call, @NonNull Throwable t) {
                        if (ApiClient.isUnauthorized(t)) {
                            ApiClient.handleUnauthorized(requireContext());
                            return;
                        }
                        rollback(appointment);
                    }
                });
    }

    private void rollback(Models.Appointment original) {
        adapter.replace(original);
        if (getView() != null) {
            Snackbar.make(getView(), R.string.action_failed_rollback, Snackbar.LENGTH_SHORT).show();
        }
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
        c.patientName = a.patientName;
        c.patientPhone = a.patientPhone;
        return c;
    }
}
