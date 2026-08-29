package ir.artor.badoki.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import ir.artor.badoki.BadokiApp;
import ir.artor.badoki.MainActivity;
import ir.artor.badoki.R;
import ir.artor.badoki.adapter.DoctorAdapter;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.util.Fmt;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * جستجو و فیلتر پزشکان بر اساس تخصص، شهر و بیمارستان.
 * هر ردیف فیلتر مستقل است و می‌توانند هم‌زمان با هم اعمال شوند (AND).
 */
public class DoctorsFragment extends Fragment {

    private final Handler debouncer = new Handler(Looper.getMainLooper());
    private EditText searchField;
    private ChipGroup specialtyChips;
    private ChipGroup cityChips;
    private ChipGroup hospitalChips;
    private SwipeRefreshLayout swipe;
    private RecyclerView doctorsRv;
    private View emptyView, loadingView, errorView, resultBar;
    private TextView resultCount;
    private TextView clearFilters;
    private DoctorAdapter adapter;

    private String query = "";
    private String specialty = null;
    private String city = null;
    private String hospital = null;
    private boolean suppressReload = false;
    private Call<Models.DoctorsPage> inFlight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_doctors, container, false);

        searchField = root.findViewById(R.id.search_field);
        specialtyChips = root.findViewById(R.id.specialty_chips);
        cityChips = root.findViewById(R.id.city_chips);
        hospitalChips = root.findViewById(R.id.hospital_chips);
        swipe = root.findViewById(R.id.swipe);
        doctorsRv = root.findViewById(R.id.doctors_rv);
        emptyView = root.findViewById(R.id.empty_view);
        loadingView = root.findViewById(R.id.loading_view);
        errorView = root.findViewById(R.id.error_view);
        resultBar = root.findViewById(R.id.result_bar);
        resultCount = root.findViewById(R.id.result_count);
        clearFilters = root.findViewById(R.id.clear_filters);
        root.findViewById(R.id.retry_btn).setOnClickListener(v -> loadAll());
        clearFilters.setOnClickListener(v -> resetFilters());

        doctorsRv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DoctorAdapter(false, doctor ->
                ((MainActivity) requireActivity()).open(
                        DoctorDetailFragment.newInstance(doctor.id), "", true));
        doctorsRv.setAdapter(adapter);

        swipe.setColorSchemeResources(R.color.primary);
        swipe.setOnRefreshListener(this::loadAll);

        searchField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (suppressReload) return;
                debouncer.removeCallbacksAndMessages(null);
                debouncer.postDelayed(() -> {
                    query = s == null ? "" : s.toString().trim();
                    loadDoctors(true);
                }, 400);
            }
        });

        loadAll();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        debouncer.removeCallbacksAndMessages(null);
        if (inFlight != null) inFlight.cancel();
    }

    private void loadAll() {
        loadMeta();
        loadDoctors(false);
    }

    private void loadMeta() {
        fetchChips(BadokiApp.api().specialties(), specialtyChips, value -> specialty = value);
        fetchChips(BadokiApp.api().cities(), cityChips, value -> city = value);
        fetchChips(BadokiApp.api().hospitals(), hospitalChips, value -> hospital = value);
    }

    private void fetchChips(Call<List<String>> call, ChipGroup group, Consumer<String> onSelect) {
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call,
                                   @NonNull Response<List<String>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    buildFilterChips(group, response.body(), onSelect);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                // فیلترها اختیاری‌اند؛ لیست پزشکان بدون آن‌ها هم کار می‌کند
            }
        });
    }

    /** ساخت تراشه‌های فیلتر با گزینه «همه» — هر ChipGroup جداگانه پر می‌شود */
    private void buildFilterChips(ChipGroup group, List<String> values,
                                  Consumer<String> onSelect) {
        if (group.getChildCount() > 0) return;
        group.removeAllViews();
        group.setSingleSelection(true);
        group.setSelectionRequired(true);

        group.addView(makeChip(getString(R.string.filter_all), true));
        if (values != null) {
            for (String value : values) {
                if (value == null || value.trim().isEmpty()) continue;
                group.addView(makeChip(value, false));
            }
        }

        group.setOnCheckedStateChangeListener((chipGroup, checkedIds) -> {
            if (suppressReload) return;
            int id = checkedIds.isEmpty() ? -1 : checkedIds.get(0);
            Chip checked = id == -1 ? null : chipGroup.findViewById(id);
            if (checked == null || checked.getText() == null
                    || getString(R.string.filter_all).contentEquals(checked.getText())) {
                onSelect.accept(null);
            } else {
                onSelect.accept(checked.getText().toString());
            }
            loadDoctors(true);
        });
    }

    private Chip makeChip(String text, boolean checked) {
        Chip chip = new Chip(requireContext(), null,
                com.google.android.material.R.attr.filterChipStyle);
        chip.setText(text);
        chip.setCheckable(true);
        chip.setChecked(checked);
        chip.setCheckedIconVisible(false);
        chip.setEnsureMinTouchTargetSize(false);
        chip.setChipMinHeight(dp(32));
        chip.setChipStrokeWidth(0);
        chip.setChipCornerRadius(dp(16));
        chip.setTextSize(12);
        chip.setChipBackgroundColor(ContextCompat.getColorStateList(
                requireContext(), R.color.filter_chip_bg));
        chip.setTextColor(ContextCompat.getColorStateList(
                requireContext(), R.color.filter_chip_text));
        return chip;
    }

    private float dp(int value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    private void resetFilters() {
        suppressReload = true;
        debouncer.removeCallbacksAndMessages(null);
        query = "";
        specialty = null;
        city = null;
        hospital = null;
        searchField.setText("");
        selectAll(specialtyChips);
        selectAll(cityChips);
        selectAll(hospitalChips);
        suppressReload = false;
        loadDoctors(true);
    }

    private void selectAll(ChipGroup group) {
        if (group.getChildCount() > 0) {
            ((Chip) group.getChildAt(0)).setChecked(true);
        }
    }

    private boolean hasActiveFilters() {
        return !query.isEmpty() || specialty != null || city != null || hospital != null;
    }

    private void loadDoctors(boolean silent) {
        if (!silent) {
            loadingView.setVisibility(View.VISIBLE);
            errorView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        }
        if (inFlight != null) inFlight.cancel();
        inFlight = BadokiApp.api().doctors(
                query.isEmpty() ? null : query,
                specialty, city, hospital, 0, 50);
        inFlight.enqueue(new Callback<Models.DoctorsPage>() {
            @Override
            public void onResponse(@NonNull Call<Models.DoctorsPage> call,
                                   @NonNull Response<Models.DoctorsPage> response) {
                if (!isAdded()) return;
                swipe.setRefreshing(false);
                loadingView.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Models.DoctorsPage page = response.body();
                    List<Models.Doctor> items = page.items;
                    adapter.submit(items);
                    boolean empty = items == null || items.isEmpty();
                    emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
                    doctorsRv.setVisibility(empty ? View.GONE : View.VISIBLE);
                    long total = page.total;
                    resultBar.setVisibility(View.VISIBLE);
                    resultCount.setText(getString(R.string.doctors_result_count, Fmt.fa(total)));
                    clearFilters.setVisibility(hasActiveFilters() ? View.VISIBLE : View.GONE);
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.DoctorsPage> call, @NonNull Throwable t) {
                if (call.isCanceled() || !isAdded()) return;
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

    private void showError() {
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        doctorsRv.setVisibility(View.GONE);
        resultBar.setVisibility(View.GONE);
    }
}
