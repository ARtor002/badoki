package ir.artor.badoki.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ir.artor.badoki.BadokiApp;
import ir.artor.badoki.R;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.util.Fmt;
import ir.artor.badoki.util.Jalali;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** برگه تغییر زمان نوبت: انتخاب تاریخ و ساعت خالی */
public class RescheduleSheet extends BottomSheetDialogFragment {

    private static final String ARG_APPOINTMENT = "appointment";
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private Models.Appointment appointment;
    private Set<String> availableDays = new HashSet<>();

    private ChipGroup dateChips, slotsChips;
    private ProgressBar slotsLoading;
    private TextView slotsError;
    private Button confirmBtn;
    private LocalDate selectedDate;
    private String selectedTime;

    public static RescheduleSheet newInstance(Models.Appointment appointment) {
        RescheduleSheet sheet = new RescheduleSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_APPOINTMENT, appointment);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_reschedule, container, false);

        appointment = (Models.Appointment) getArguments().getSerializable(ARG_APPOINTMENT);
        dateChips = root.findViewById(R.id.rs_date_chips);
        slotsChips = root.findViewById(R.id.rs_slots_chips);
        slotsLoading = root.findViewById(R.id.rs_slots_loading);
        slotsError = root.findViewById(R.id.rs_slots_error);
        confirmBtn = root.findViewById(R.id.rs_confirm);
        TextView errorText = root.findViewById(R.id.rs_error);

        confirmBtn.setEnabled(false);
        confirmBtn.setOnClickListener(v -> {
            if (selectedDate == null || selectedTime == null) return;
            dismiss();
            androidx.fragment.app.Fragment parent = getParentFragment();
            if (parent instanceof AppointmentsFragment) {
                ((AppointmentsFragment) parent).onRescheduleChosen(
                        appointment, selectedDate.format(ISO), selectedTime);
            }
        });
        root.findViewById(R.id.rs_cancel).setOnClickListener(v -> dismiss());

        // دریافت اطلاعات پزشک برای روزهای کاری
        BadokiApp.api().doctor(appointment.doctorId).enqueue(new Callback<Models.Doctor>() {
            @Override
            public void onResponse(@NonNull Call<Models.Doctor> call,
                                   @NonNull Response<Models.Doctor> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> days = response.body().availableDays;
                    if (days != null) availableDays.addAll(days);
                    buildDateChips();
                } else {
                    slotsError.setText(getString(R.string.error_generic));
                    slotsError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.Doctor> call, @NonNull Throwable t) {
                slotsError.setText(getString(R.string.error_network));
                slotsError.setVisibility(View.VISIBLE);
            }
        });
        return root;
    }

    private void buildDateChips() {
        dateChips.removeAllViews();
        dateChips.setSingleSelection(true);
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 14; i++) {
            LocalDate date = today.plusDays(i);
            Chip chip = new Chip(requireContext());
            String label = i == 0 ? getString(R.string.today)
                    : i == 1 ? getString(R.string.tomorrow)
                    : Jalali.weekdayName(date.getDayOfWeek()) + " " + Fmt.fa(Jalali.toJalali(date).jd);
            chip.setText(label);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setEnabled(availableDays.contains(date.getDayOfWeek().name()));
            dateChips.addView(chip);
        }
        dateChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = checkedIds.isEmpty() ? -1 : checkedIds.get(0);
            Chip checked = id == -1 ? null : group.findViewById(id);
            if (checked == null) return;
            int index = group.indexOfChild(checked);
            selectedDate = today.plusDays(index);
            selectedTime = null;
            confirmBtn.setEnabled(false);
            loadSlots(selectedDate);
        });
    }

    private void loadSlots(LocalDate date) {
        slotsLoading.setVisibility(View.VISIBLE);
        slotsChips.setVisibility(View.GONE);
        slotsError.setVisibility(View.GONE);

        BadokiApp.api().slots(appointment.doctorId, date.format(ISO))
                .enqueue(new Callback<List<Models.Slot>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Models.Slot>> call,
                                           @NonNull Response<List<Models.Slot>> response) {
                        slotsLoading.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            renderSlots(response.body());
                        } else {
                            slotsError.setText(ApiClient.errorMessage(
                                    new retrofit2.HttpException(response)));
                            slotsError.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Models.Slot>> call, @NonNull Throwable t) {
                        slotsLoading.setVisibility(View.GONE);
                        slotsError.setText(getString(R.string.error_network));
                        slotsError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void renderSlots(List<Models.Slot> slotList) {
        slotsChips.removeAllViews();
        slotsChips.setSingleSelection(true);
        boolean any = false;
        boolean isToday = LocalDate.now().equals(selectedDate);
        LocalTime now = LocalTime.now();
        for (Models.Slot slot : slotList) {
            Chip chip = new Chip(requireContext());
            // نمایش با ارقام فارسی، اما مقدار اصلی (لاتین) در tag نگه داشته می‌شود
            chip.setText(Fmt.faTime(slot.time));
            chip.setTag(slot.time);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            // اسلات‌های گذشته امروز هم غیرفعال‌اند
            boolean passed = isToday && LocalTime.parse(slot.time).isBefore(now);
            chip.setEnabled(slot.available && !passed);
            if (slot.available && !passed) any = true;
            slotsChips.addView(chip);
        }
        slotsChips.setVisibility(View.VISIBLE);
        if (!any) {
            slotsError.setText(R.string.no_slots_title);
            slotsError.setVisibility(View.VISIBLE);
        }
        slotsChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = checkedIds.isEmpty() ? -1 : checkedIds.get(0);
            Chip checked = id == -1 ? null : group.findViewById(id);
            if (checked != null && checked.getTag() != null) {
                // مقدار اصلی (مثل 09:30) برای ارسال به سرور — نه متن فارسی نمایشی
                selectedTime = (String) checked.getTag();
                confirmBtn.setEnabled(true);
            }
        });
    }
}
