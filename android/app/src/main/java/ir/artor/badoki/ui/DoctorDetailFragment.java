package ir.artor.badoki.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ir.artor.badoki.BadokiApp;
import ir.artor.badoki.MainActivity;
import ir.artor.badoki.R;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.adapter.ReviewAdapter;
import ir.artor.badoki.util.Avatar;
import ir.artor.badoki.util.Fmt;
import ir.artor.badoki.util.Jalali;
import ir.artor.badoki.util.SessionManager;
import ir.artor.badoki.util.StarRow;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** صفحه جزئیات پزشک: اطلاعات، انتخاب تاریخ و ساعت، رزرو نوبت */
public class DoctorDetailFragment extends Fragment {

    private static final String ARG_DOCTOR_ID = "doctor_id";
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    public static DoctorDetailFragment newInstance(long doctorId) {
        DoctorDetailFragment f = new DoctorDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DOCTOR_ID, doctorId);
        f.setArguments(args);
        return f;
    }

    private long doctorId;
    private Models.Doctor doctor;

    private View loadingView, errorView, contentView, slotsLoading;
    private TextView slotsError;
    private ChipGroup daysChips, dateChips, slotsChips;
    private Button bookBtn;
    private TextView bookHint;

    private LocalDate selectedDate;
    private String selectedTime;
    private List<Models.Slot> slots;

    // بخش امتیاز و نظر
    private TextView reviewsAvg, reviewsCount, reviewsEmpty, reviewsAddBtn;
    private LinearLayout reviewsAvgStars;
    private RecyclerView reviewsRv;
    private ReviewAdapter reviewsAdapter;
    private boolean busy;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_doctor_detail, container, false);

        doctorId = getArguments() != null ? getArguments().getLong(ARG_DOCTOR_ID, -1) : -1;

        loadingView = root.findViewById(R.id.detail_loading);
        errorView = root.findViewById(R.id.detail_error);
        contentView = root.findViewById(R.id.detail_content);
        root.findViewById(R.id.retry_btn).setOnClickListener(v -> loadDoctor());

        daysChips = root.findViewById(R.id.days_chips);
        dateChips = root.findViewById(R.id.date_chips);
        slotsChips = root.findViewById(R.id.slots_chips);
        slotsLoading = root.findViewById(R.id.slots_loading);
        slotsError = root.findViewById(R.id.slots_error);
        bookBtn = root.findViewById(R.id.book_btn);
        bookHint = root.findViewById(R.id.book_hint);
        bookBtn.setOnClickListener(v -> onBookClicked());

        reviewsAvg = root.findViewById(R.id.reviews_avg);
        reviewsAvgStars = root.findViewById(R.id.reviews_avg_stars);
        reviewsCount = root.findViewById(R.id.reviews_count);
        reviewsEmpty = root.findViewById(R.id.reviews_empty);
        reviewsAddBtn = root.findViewById(R.id.reviews_add_btn);
        reviewsRv = root.findViewById(R.id.reviews_rv);
        reviewsRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        reviewsRv.setNestedScrollingEnabled(false);
        reviewsAdapter = new ReviewAdapter(SessionManager.userId(), review ->
                confirmDeleteReview(review));
        reviewsRv.setAdapter(reviewsAdapter);

        reviewsAddBtn.setOnClickListener(v -> openReviewSheet());

        loadDoctor();
        return root;
    }

    private void loadDoctor() {
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        contentView.setVisibility(View.GONE);

        BadokiApp.api().doctor(doctorId).enqueue(new Callback<Models.Doctor>() {
            @Override
            public void onResponse(@NonNull Call<Models.Doctor> call,
                                   @NonNull Response<Models.Doctor> response) {
                loadingView.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    doctor = response.body();
                    renderDoctor(doctor);
                    buildDateChips(doctor);
                    loadReviews();
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

    private void renderDoctor(Models.Doctor d) {
        View root = getView();
        if (root == null) return;
        TextView avatar = root.findViewById(R.id.avatar);
        avatar.setText(Avatar.initials(d.fullName));
        avatar.setBackgroundTintList(Avatar.tintFor(requireContext(), d.fullName));
        avatar.setTextColor(Avatar.textColor(requireContext()));

        ((TextView) root.findViewById(R.id.name)).setText(d.fullName);
        ((TextView) root.findViewById(R.id.specialty)).setText(d.specialty);
        ((TextView) root.findViewById(R.id.rating)).setText(Fmt.rating(d.rating));
        ((TextView) root.findViewById(R.id.reviews)).setText("(" + Fmt.fa(d.reviewCount) + " "
                + getString(R.string.reviews) + ")");
        ((TextView) root.findViewById(R.id.exp)).setText(Fmt.fa(d.experienceYears) + " "
                + getString(R.string.years_exp));
        ((TextView) root.findViewById(R.id.location)).setText(
                (d.hospitalName == null || d.hospitalName.isEmpty() ? "" : d.hospitalName + "، ")
                        + d.city);
        ((TextView) root.findViewById(R.id.address)).setText(d.address);
        ((TextView) root.findViewById(R.id.bio)).setText(d.bio);
        ((TextView) root.findViewById(R.id.price)).setText(Fmt.toman(d.visitPrice));

        // بخش امتیاز و نظر
        reviewsAvg.setText(Fmt.rating(d.rating));
        StarRow.populateForRating(requireContext(), reviewsAvgStars, d.rating);
        reviewsCount.setText("(" + Fmt.fa(d.reviewCount) + " " + getString(R.string.reviews) + ")");

        daysChips.removeAllViews();
        for (String day : d.availableDays) {
            Chip chip = new Chip(requireContext());
            chip.setText(dayNameFa(day));
            chip.setClickable(false);
            chip.setCheckable(false);
            chip.setCheckedIconVisible(false);
            daysChips.addView(chip);
        }
    }

    private String dayNameFa(String dayOfWeek) {
        int res;
        switch (dayOfWeek) {
            case "SATURDAY": res = R.string.weekday_SATURDAY; break;
            case "SUNDAY": res = R.string.weekday_SUNDAY; break;
            case "MONDAY": res = R.string.weekday_MONDAY; break;
            case "TUESDAY": res = R.string.weekday_TUESDAY; break;
            case "WEDNESDAY": res = R.string.weekday_WEDNESDAY; break;
            case "THURSDAY": res = R.string.weekday_THURSDAY; break;
            default: res = R.string.weekday_FRIDAY;
        }
        return getString(res);
    }

    /** تراشه‌های ۱۴ روز آینده؛ روزهای غیرکاری غیرفعال */
    private void buildDateChips(Models.Doctor d) {
        dateChips.removeAllViews();
        dateChips.setSingleSelection(true);

        Set<String> availableDays = new HashSet<>();
        if (d.availableDays != null) availableDays.addAll(d.availableDays);

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 14; i++) {
            LocalDate date = today.plusDays(i);
            Chip chip = new Chip(requireContext());
            String label;
            if (i == 0) label = getString(R.string.today);
            else if (i == 1) label = getString(R.string.tomorrow);
            else label = Jalali.weekdayName(date.getDayOfWeek()) + " "
                        + Fmt.fa(Jalali.toJalali(date).jd);
            chip.setText(label);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);

            boolean available = availableDays.contains(date.getDayOfWeek().name());
            chip.setEnabled(available);
            if (!available) {
                chip.setTag("off");
            }
            dateChips.addView(chip);
        }

        dateChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = checkedIds.isEmpty() ? -1 : checkedIds.get(0);
            Chip checked = id == -1 ? null : group.findViewById(id);
            if (checked == null) return;
            int index = group.indexOfChild(checked);
            selectedDate = today.plusDays(index);
            selectedTime = null;
            updateBookButton();
            loadSlots(selectedDate);
        });

        // انتخاب اولین روز کاری
        for (int i = 0; i < dateChips.getChildCount(); i++) {
            Chip chip = (Chip) dateChips.getChildAt(i);
            if (chip.isEnabled()) {
                chip.setChecked(true);
                break;
            }
        }
    }

    private void loadSlots(LocalDate date) {
        slotsLoading.setVisibility(View.VISIBLE);
        slotsError.setVisibility(View.GONE);
        slotsChips.setVisibility(View.GONE);

        BadokiApp.api().slots(doctorId, date.format(ISO))
                .enqueue(new Callback<List<Models.Slot>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Models.Slot>> call,
                                           @NonNull Response<List<Models.Slot>> response) {
                        slotsLoading.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            slots = response.body();
                            renderSlots(slots);
                        } else {
                            slotsError.setText(ApiClient.errorMessage(
                                    new retrofit2.HttpException(response)));
                            slotsError.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Models.Slot>> call, @NonNull Throwable t) {
                        slotsLoading.setVisibility(View.GONE);
                        if (ApiClient.isUnauthorized(t)) {
                            ApiClient.handleUnauthorized(requireContext());
                            return;
                        }
                        slotsError.setText(getString(R.string.error_network));
                        slotsError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void renderSlots(List<Models.Slot> slotList) {
        slotsChips.removeAllViews();
        slotsChips.setSingleSelection(true);
        boolean anyAvailable = false;
        for (Models.Slot slot : slotList) {
            Chip chip = new Chip(requireContext());
            // نمایش با ارقام فارسی، اما مقدار اصلی (لاتین) در tag نگه داشته می‌شود
            chip.setText(Fmt.faTime(slot.time));
            chip.setTag(slot.time);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setEnabled(slot.available);
            if (slot.available) anyAvailable = true;
            slotsChips.addView(chip);
        }
        slotsChips.setVisibility(View.VISIBLE);
        if (!anyAvailable) {
            slotsError.setText(R.string.no_slots_title);
            slotsError.setVisibility(View.VISIBLE);
        }

        slotsChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = checkedIds.isEmpty() ? -1 : checkedIds.get(0);
            Chip checked = id == -1 ? null : group.findViewById(id);
            if (checked != null && checked.getTag() != null) {
                // مقدار اصلی (مثل 09:30) برای ارسال به سرور — نه متن فارسی نمایشی
                selectedTime = (String) checked.getTag();
                updateBookButton();
            }
        });
        updateBookButton();
    }

    private void updateBookButton() {
        boolean ready = selectedDate != null && selectedTime != null && !busy;
        bookBtn.setEnabled(ready);
        if (selectedDate != null && selectedTime != null) {
            bookHint.setText(Fmt.dateFull(requireContext(), selectedDate.format(ISO))
                    + " — " + Fmt.faTime(selectedTime));
            bookHint.setVisibility(View.VISIBLE);
        } else {
            bookHint.setText(R.string.select_slot_hint);
            bookHint.setVisibility(View.VISIBLE);
        }
    }

    // ---------- امتیاز و نظر ----------

    private void loadReviews() {
        BadokiApp.api().reviews(doctorId).enqueue(new Callback<List<Models.Review>>() {
            @Override
            public void onResponse(@NonNull Call<List<Models.Review>> call,
                                   @NonNull Response<List<Models.Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reviewsAdapter.submit(response.body());
                    boolean empty = response.body().isEmpty();
                    reviewsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                    reviewsRv.setVisibility(empty ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Models.Review>> call, @NonNull Throwable t) {
                if (ApiClient.isUnauthorized(t)) {
                    ApiClient.handleUnauthorized(requireContext());
                }
            }
        });
    }

    private void openReviewSheet() {
        ReviewSheet sheet = ReviewSheet.newInstance(doctorId);
        sheet.setListener(() -> {
            // بعد از ثبت نظر: امتیاز و نظرات را تازه کن
            loadDoctor();
            loadReviews();
        });
        sheet.show(getChildFragmentManager(), "review");
    }

    private void confirmDeleteReview(Models.Review review) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.reviews_delete_title)
                .setMessage(R.string.reviews_delete_message)
                .setPositiveButton(R.string.confirm, (d, w) -> {
                    BadokiApp.api().deleteReview(review.id).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            loadDoctor();
                            loadReviews();
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            if (ApiClient.isUnauthorized(t)) {
                                ApiClient.handleUnauthorized(requireContext());
                            }
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void onBookClicked() {
        if (doctor == null || selectedDate == null || selectedTime == null) return;
        BookSheet.newInstance(doctor, selectedDate.format(ISO), selectedTime)
                .show(getChildFragmentManager(), "book");
    }

    /** بعد از رزور موفق یا رزرو دوباره */
    public void onBookResult(boolean bookedAgain) {
        if (bookedAgain) {
            // پاک کردن انتخاب قبلی
            selectedTime = null;
            slotsChips.clearCheck();
            updateBookButton();
        } else {
            ((MainActivity) requireActivity()).open(
                    new AppointmentsFragment(), getString(R.string.nav_appointments), false);
        }
    }
}
