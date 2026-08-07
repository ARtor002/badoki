package ir.artor.badoki.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ir.artor.badoki.BadokiApp;
import ir.artor.badoki.MainActivity;
import ir.artor.badoki.R;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.util.Fmt;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** داشبورد ادمین: آمار کل سیستم و دسترسی سریع */
public class AdminDashboardFragment extends Fragment {

    private View loadingView, errorView, contentRoot;
    private TextView statDoctors, statPatients, statAppointments, statPending;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        loadingView = root.findViewById(R.id.loading_view);
        errorView = root.findViewById(R.id.error_view);
        contentRoot = root.findViewById(R.id.content_root);
        root.findViewById(R.id.retry_btn).setOnClickListener(v -> load());

        statDoctors = root.findViewById(R.id.stat_doctors_value);
        statPatients = root.findViewById(R.id.stat_patients_value);
        statAppointments = root.findViewById(R.id.stat_appointments_value);
        statPending = root.findViewById(R.id.stat_pending_value);

        root.findViewById(R.id.btn_manage_doctors).setOnClickListener(v ->
                ((MainActivity) requireActivity()).open(
                        new AdminDoctorsFragment(), getString(R.string.nav_admin_doctors), false));
        root.findViewById(R.id.btn_manage_users).setOnClickListener(v ->
                ((MainActivity) requireActivity()).open(
                        new AdminUsersFragment(), getString(R.string.nav_admin_users), false));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        contentRoot.setVisibility(View.GONE);

        BadokiApp.api().adminStats().enqueue(new Callback<Models.AdminStats>() {
            @Override
            public void onResponse(@NonNull Call<Models.AdminStats> call,
                                   @NonNull Response<Models.AdminStats> response) {
                loadingView.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Models.AdminStats s = response.body();
                    statDoctors.setText(Fmt.fa(s.doctors));
                    statPatients.setText(Fmt.fa(s.patients));
                    statAppointments.setText(Fmt.fa(s.appointments));
                    statPending.setText(Fmt.fa(s.pendingAppointments));
                    contentRoot.setVisibility(View.VISIBLE);
                } else {
                    errorView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.AdminStats> call, @NonNull Throwable t) {
                loadingView.setVisibility(View.GONE);
                if (ApiClient.isUnauthorized(t)) {
                    ApiClient.handleUnauthorized(requireContext());
                    return;
                }
                errorView.setVisibility(View.VISIBLE);
            }
        });
    }
}
