package ir.artor.badoki.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** جستجو و فیلتر پزشکان بر اساس تخصص و شهر */
public class DoctorsFragment extends Fragment {

    private final Handler debouncer = new Handler(Looper.getMainLooper());
    private EditText searchField;
    private ChipGroup specialtyChips;
    private ChipGroup cityChips;
    private SwipeRefreshLayout swipe;
    private RecyclerView doctorsRv;
    private View emptyView, loadingView, errorView;
    private DoctorAdapter adapter;

    private String query = "";
    private String specialty = null;
    private String city = null;
    private boolean metaLoaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_doctors, container, false);

        searchField = root.findViewById(R.id.search_field);
        specialtyChips = root.findViewById(R.id.specialty_chips);
        cityChips = root.findViewById(R.id.city_chips);
        swipe = root.findViewById(R.id.swipe);
        doctorsRv = root.findViewById(R.id.doctors_rv);
        emptyView = root.findViewById(R.id.empty_view);
        loadingView = root.findViewById(R.id.loading_view);
        errorView = root.findViewById(R.id.error_view);
        root.findViewById(R.id.retry_btn).setOnClickListener(v -> loadAll());

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

    private void loadAll() {
        loadMeta();
        loadDoctors(false);
    }

    private void loadMeta() {
        Call<List<String>> specCall = BadokiApp.api().specialties();
        Call<List<String>> cityCall = BadokiApp.api().cities();
        specCall.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call,
                                   @NonNull Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    buildFilterChips(specialtyChips, response.body(), value ->
                            specialty = value);
                }
            }
            @Override public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {}
        });
        cityCall.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call,
                                   @NonNull Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    buildFilterChips(cityChips, response.body(), value ->
                            city = value);
                }
            }
            @Override public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {}
        });
    }

    /** ساخت تراشه‌های فیلتر با گزینه «همه» */
    private void buildFilterChips(ChipGroup group, List<String> values,
                                  java.util.function.Consumer<String> onSelect) {
        if (metaLoaded || group.getChildCount() > 0) return;
        group.removeAllViews();
        group.setSingleSelection(true);

        Chip all = new Chip(requireContext());
        all.setText(R.string.filter_all);
        all.setCheckable(true);
        all.setChecked(true);
        all.setCheckedIconVisible(false);
        group.addView(all);

        for (String value : values) {
            Chip chip = new Chip(requireContext());
            chip.setText(value);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            group.addView(chip);
        }

        group.setOnCheckedStateChangeListener((chipGroup, checkedIds) -> {
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
        metaLoaded = true;
    }

    private void loadDoctors(boolean silent) {
        if (!silent) {
            loadingView.setVisibility(View.VISIBLE);
            errorView.setVisibility(View.GONE);
        }
        BadokiApp.api().doctors(query.isEmpty() ? null : query,
                specialty, city, 0, 50).enqueue(new Callback<Models.DoctorsPage>() {
            @Override
            public void onResponse(@NonNull Call<Models.DoctorsPage> call,
                                   @NonNull Response<Models.DoctorsPage> response) {
                swipe.setRefreshing(false);
                loadingView.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Models.Doctor> items = response.body().items;
                    adapter.submit(items);
                    boolean empty = items == null || items.isEmpty();
                    emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
                    doctorsRv.setVisibility(empty ? View.GONE : View.VISIBLE);
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.DoctorsPage> call, @NonNull Throwable t) {
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
    }
}
