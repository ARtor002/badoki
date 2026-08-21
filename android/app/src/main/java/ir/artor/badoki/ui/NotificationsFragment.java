package ir.artor.badoki.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import ir.artor.badoki.BadokiApp;
import ir.artor.badoki.MainActivity;
import ir.artor.badoki.R;
import ir.artor.badoki.adapter.NotificationAdapter;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** مرکز اطلاع‌رسانی — لیست اعلان‌ها با پولینگ هر ۳۰ ثانیه */
public class NotificationsFragment extends Fragment {

    private final Handler poller = new Handler(Looper.getMainLooper());
    private final Runnable pollTask = new Runnable() {
        @Override
        public void run() {
            load();
            poller.postDelayed(this, 30_000);
        }
    };

    private SwipeRefreshLayout swipe;
    private RecyclerView rv;
    private View emptyView, loadingView, errorView;
    private NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        swipe = root.findViewById(R.id.swipe);
        rv = root.findViewById(R.id.notif_rv);
        emptyView = root.findViewById(R.id.empty_view);
        loadingView = root.findViewById(R.id.loading_view);
        errorView = root.findViewById(R.id.error_view);
        root.findViewById(R.id.retry_btn).setOnClickListener(v -> load());

        root.findViewById(R.id.read_all_btn).setOnClickListener(v -> markAllRead());

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notification -> markRead(notification.id));
        rv.setAdapter(adapter);

        swipe.setColorSchemeResources(R.color.primary);
        swipe.setOnRefreshListener(this::load);

        load();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        poller.removeCallbacks(pollTask);
        poller.postDelayed(pollTask, 30_000);
    }

    @Override
    public void onPause() {
        super.onPause();
        poller.removeCallbacks(pollTask);
    }

    private void load() {
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        BadokiApp.api().notifications().enqueue(new Callback<List<Models.Notification>>() {
            @Override
            public void onResponse(@NonNull Call<List<Models.Notification>> call,
                                   @NonNull Response<List<Models.Notification>> response) {
                swipe.setRefreshing(false);
                loadingView.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.submit(response.body());
                    boolean empty = response.body().isEmpty();
                    emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
                    rv.setVisibility(empty ? View.GONE : View.VISIBLE);
                    refreshBadge();
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Models.Notification>> call, @NonNull Throwable t) {
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
        rv.setVisibility(View.GONE);
    }

    private void markRead(long id) {
        BadokiApp.api().markNotificationRead(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    for (Models.Notification n : adapterItems()) {
                        if (n.id == id) n.read = true;
                    }
                    adapter.notifyDataSetChanged();
                    refreshBadge();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (ApiClient.isUnauthorized(t)) {
                    ApiClient.handleUnauthorized(requireContext());
                }
            }
        });
    }

    private void markAllRead() {
        BadokiApp.api().markAllNotificationsRead().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    load();
                    refreshBadge();
                    if (getView() != null) {
                        Snackbar.make(getView(), R.string.notifications_read_all, Snackbar.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (ApiClient.isUnauthorized(t)) {
                    ApiClient.handleUnauthorized(requireContext());
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private List<Models.Notification> adapterItems() {
        try {
            java.lang.reflect.Field f = adapter.getClass().getDeclaredField("items");
            f.setAccessible(true);
            return (List<Models.Notification>) f.get(adapter);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    private void refreshBadge() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshUnreadBadge();
        }
    }
}
