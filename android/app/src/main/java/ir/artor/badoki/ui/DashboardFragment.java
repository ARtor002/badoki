package ir.artor.badoki.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ir.artor.badoki.BadokiApp;
import ir.artor.badoki.MainActivity;
import ir.artor.badoki.R;
import ir.artor.badoki.adapter.DoctorAdapter;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.util.Avatar;
import ir.artor.badoki.util.Fmt;
import ir.artor.badoki.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** صفحه اصلی: خوش‌آمد، آمار، نوبت بعدی و پزشکان برتر */
public class DashboardFragment extends Fragment {

    private View loadingView, errorView, contentRoot;
    private View nextCard;
    private TextView greeting, todayDate;
    private TextView statUpcoming, statCompleted, statCanceled;
    private TextView nextAvatar, nextName, nextSpecialty, nextStatus, nextDatetime, nextHospital;
    private RecyclerView topDoctorsRv;
    private DoctorAdapter topAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        loadingView = root.findViewById(R.id.loading_view);
        errorView = root.findViewById(R.id.error_view);
        contentRoot = root.findViewById(R.id.content_root);
        root.findViewById(R.id.retry_btn).setOnClickListener(v -> load());

        greeting = root.findViewById(R.id.greeting_text);
        todayDate = root.findViewById(R.id.today_date);
        statUpcoming = root.findViewById(R.id.stat_upcoming_value);
        statCompleted = root.findViewById(R.id.stat_completed_value);
        statCanceled = root.findViewById(R.id.stat_canceled_value);

        nextCard = root.findViewById(R.id.next_card);
        nextAvatar = root.findViewById(R.id.next_avatar);
        nextName = root.findViewById(R.id.next_name);
        nextSpecialty = root.findViewById(R.id.next_specialty);
        nextStatus = root.findViewById(R.id.next_status);
        nextDatetime = root.findViewById(R.id.next_datetime);
        nextHospital = root.findViewById(R.id.next_hospital);

        topDoctorsRv = root.findViewById(R.id.top_doctors_rv);
        topDoctorsRv.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        topAdapter = new DoctorAdapter(true, doctor -> openDetail(doctor.id));
        topDoctorsRv.setAdapter(topAdapter);

        root.findViewById(R.id.stat_upcoming_card).setOnClickListener(v -> openAppointments());
        root.findViewById(R.id.btn_find_doctor).setOnClickListener(v -> openDoctors());
        root.findViewById(R.id.btn_my_appointments).setOnClickListener(v -> openAppointments());
        root.findViewById(R.id.see_all_btn).setOnClickListener(v -> openDoctors());
        root.findViewById(R.id.next_btn).setOnClickListener(v -> openAppointments());

        String firstName = SessionManager.name();
        if (firstName.contains(" ")) firstName = firstName.substring(0, firstName.indexOf(' '));
        greeting.setText(getString(R.string.dashboard_greeting, firstName));
        todayDate.setText(Fmt.todayFull(requireContext()));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        showState("loading");
        Call<Models.Dashboard> dashCall = BadokiApp.api().dashboard();
        Call<Models.DoctorsPage> topCall = BadokiApp.api().doctors(null, null, null, 0, 5);

        dashCall.enqueue(new Callback<Models.Dashboard>() {
            @Override
            public void onResponse(@NonNull Call<Models.Dashboard> call,
                                   @NonNull Response<Models.Dashboard> response) {
                if (response.isSuccessful() && response.body() != null) {
                    renderDashboard(response.body());
                    showState("content");
                } else {
                    showState("error");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.Dashboard> call, @NonNull Throwable t) {
                if (ApiClient.isUnauthorized(t)) {
                    ApiClient.handleUnauthorized(requireContext());
                    return;
                }
                showState("error");
            }
        });

        topCall.enqueue(new Callback<Models.DoctorsPage>() {
            @Override
            public void onResponse(@NonNull Call<Models.DoctorsPage> call,
                                   @NonNull Response<Models.DoctorsPage> response) {
                if (response.isSuccessful() && response.body() != null) {
                    topAdapter.submit(response.body().items);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.DoctorsPage> call, @NonNull Throwable t) {
                // داشبورد اصلی هنوز نمایش داده می‌شود؛ فقط لیست برترین‌ها خالی می‌ماند
            }
        });
    }

    private void renderDashboard(Models.Dashboard d) {
        statUpcoming.setText(Fmt.fa(d.upcomingCount));
        statCompleted.setText(Fmt.fa(d.completedCount));
        statCanceled.setText(Fmt.fa(d.canceledCount));

        if (d.nextAppointment != null) {
            Models.Appointment a = d.nextAppointment;
            nextCard.setVisibility(View.VISIBLE);
            nextAvatar.setText(Avatar.initials(a.doctorName));
            nextAvatar.setBackgroundTintList(Avatar.tintFor(requireContext(), a.doctorName));
            nextAvatar.setTextColor(Avatar.textColor(requireContext()));
            nextName.setText(a.doctorName);
            nextSpecialty.setText(a.specialty);
            String status = a.status;
            int labelRes = "CONFIRMED".equals(status) ? R.string.status_CONFIRMED
                    : "PENDING".equals(status) ? R.string.status_PENDING
                    : R.string.status_PENDING;
            nextStatus.setText(labelRes);
            nextStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(statusColorBg(a.status))));
            nextStatus.setTextColor(requireContext().getColor(statusColorText(a.status)));
            nextDatetime.setText(Fmt.dateFull(requireContext(), a.date)
                    + "  •  " + Fmt.faTime(a.time));
            String hospital = (a.hospitalName == null || a.hospitalName.isEmpty())
                    ? a.city : a.hospitalName + "، " + a.city;
            nextHospital.setText(hospital);
        } else {
            nextCard.setVisibility(View.GONE);
        }
    }

    private int statusColorBg(String status) {
        switch (status == null ? "" : status) {
            case "CONFIRMED": return R.color.statusConfirmedBg;
            case "CANCELED": return R.color.statusCanceledBg;
            case "COMPLETED": return R.color.statusCompletedBg;
            default: return R.color.statusPendingBg;
        }
    }

    private int statusColorText(String status) {
        switch (status == null ? "" : status) {
            case "CONFIRMED": return R.color.statusConfirmedText;
            case "CANCELED": return R.color.statusCanceledText;
            case "COMPLETED": return R.color.statusCompletedText;
            default: return R.color.statusPendingText;
        }
    }

    private void showState(String state) {
        if (getView() == null) return;
        loadingView.setVisibility("loading".equals(state) ? View.VISIBLE : View.GONE);
        errorView.setVisibility("error".equals(state) ? View.VISIBLE : View.GONE);
        contentRoot.setVisibility("content".equals(state) ? View.VISIBLE : View.GONE);
    }

    private void openDoctors() {
        ((MainActivity) requireActivity()).open(
                new DoctorsFragment(), getString(R.string.nav_doctors), false);
    }

    private void openAppointments() {
        ((MainActivity) requireActivity()).open(
                new AppointmentsFragment(), getString(R.string.nav_appointments), false);
    }

    private void openDetail(long doctorId) {
        ((MainActivity) requireActivity()).open(
                DoctorDetailFragment.newInstance(doctorId), "", true);
    }
}
