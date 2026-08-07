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
import ir.artor.badoki.util.Avatar;
import ir.artor.badoki.util.Fmt;
import ir.artor.badoki.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** داشبورد پزشک: آمار نوبت‌ها و نوبت بعدی */
public class DoctorDashboardFragment extends Fragment {

    private View loadingView, errorView, contentRoot, nextCard;
    private TextView statToday, statUpcoming, statCompleted, statCanceled;
    private TextView nextAvatar, nextPatient, nextDatetime, nextStatus, nextDetail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_doctor_dashboard, container, false);

        loadingView = root.findViewById(R.id.loading_view);
        errorView = root.findViewById(R.id.error_view);
        contentRoot = root.findViewById(R.id.content_root);
        root.findViewById(R.id.retry_btn).setOnClickListener(v -> load());

        statToday = root.findViewById(R.id.stat_today_value);
        statUpcoming = root.findViewById(R.id.stat_upcoming_value);
        statCompleted = root.findViewById(R.id.stat_completed_value);
        statCanceled = root.findViewById(R.id.stat_canceled_value);

        nextCard = root.findViewById(R.id.next_card);
        nextAvatar = root.findViewById(R.id.next_avatar);
        nextPatient = root.findViewById(R.id.next_patient);
        nextDatetime = root.findViewById(R.id.next_datetime);
        nextStatus = root.findViewById(R.id.next_status);
        nextDetail = root.findViewById(R.id.next_detail);

        String firstName = SessionManager.name();
        if (firstName.contains(" ")) firstName = firstName.substring(0, firstName.indexOf(' '));
        ((TextView) root.findViewById(R.id.greeting_text))
                .setText(getString(R.string.doctor_greeting, firstName));
        ((TextView) root.findViewById(R.id.today_date)).setText(Fmt.todayFull(requireContext()));

        root.findViewById(R.id.btn_my_appointments).setOnClickListener(v ->
                ((MainActivity) requireActivity()).open(
                        new DoctorAppointmentsFragment(),
                        getString(R.string.nav_doctor_appointments), false));

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

        BadokiApp.api().doctorDashboard().enqueue(new Callback<Models.DoctorDashboard>() {
            @Override
            public void onResponse(@NonNull Call<Models.DoctorDashboard> call,
                                   @NonNull Response<Models.DoctorDashboard> response) {
                loadingView.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    render(response.body());
                    contentRoot.setVisibility(View.VISIBLE);
                } else {
                    errorView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.DoctorDashboard> call, @NonNull Throwable t) {
                loadingView.setVisibility(View.GONE);
                if (ApiClient.isUnauthorized(t)) {
                    ApiClient.handleUnauthorized(requireContext());
                    return;
                }
                errorView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void render(Models.DoctorDashboard d) {
        statToday.setText(Fmt.fa(d.todayCount));
        statUpcoming.setText(Fmt.fa(d.upcomingCount));
        statCompleted.setText(Fmt.fa(d.completedCount));
        statCanceled.setText(Fmt.fa(d.canceledCount));

        if (d.nextAppointment != null) {
            Models.Appointment a = d.nextAppointment;
            nextCard.setVisibility(View.VISIBLE);
            nextAvatar.setText(Avatar.initials(a.patientName == null ? "؟" : a.patientName));
            nextAvatar.setBackgroundTintList(Avatar.tintFor(requireContext(), a.patientName));
            nextAvatar.setTextColor(Avatar.textColor(requireContext()));
            nextPatient.setText(a.patientName == null ? "—" : a.patientName);
            nextDatetime.setText(Fmt.dateFull(requireContext(), a.date) + "  •  " + Fmt.faTime(a.time));
            String status = a.status;
            int labelRes = "CONFIRMED".equals(status) ? R.string.status_CONFIRMED
                    : R.string.status_PENDING;
            nextStatus.setText(labelRes);
            nextStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    requireContext().getColor("CONFIRMED".equals(status)
                            ? R.color.statusConfirmedBg : R.color.statusPendingBg)));
            nextStatus.setTextColor(requireContext().getColor("CONFIRMED".equals(status)
                    ? R.color.statusConfirmedText : R.color.statusPendingText));
            String detail = a.specialty;
            if (a.notes != null && !a.notes.isEmpty()) detail += " — " + a.notes;
            nextDetail.setText(detail);
        } else {
            nextCard.setVisibility(View.GONE);
        }
    }
}
