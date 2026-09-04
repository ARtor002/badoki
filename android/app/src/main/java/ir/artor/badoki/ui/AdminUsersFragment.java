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
import ir.artor.badoki.R;
import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;
import ir.artor.badoki.util.Avatar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** فهرست کاربران برای ادمین */
public class AdminUsersFragment extends Fragment {

    private RecyclerView rv;
    private View loadingView, errorView, emptyView;
    private UsersAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_users, container, false);

        rv = root.findViewById(R.id.users_rv);
        loadingView = root.findViewById(R.id.loading_view);
        errorView = root.findViewById(R.id.error_view);
        emptyView = root.findViewById(R.id.empty_view);
        root.findViewById(R.id.retry_btn).setOnClickListener(v -> load());

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UsersAdapter();
        rv.setAdapter(adapter);

        load();
        return root;
    }

    private void load() {
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        BadokiApp.api().adminUsers().enqueue(new Callback<List<Models.AdminUser>>() {
            @Override
            public void onResponse(@NonNull Call<List<Models.AdminUser>> call,
                                   @NonNull Response<List<Models.AdminUser>> response) {
                loadingView.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.submit(response.body());
                    boolean empty = response.body().isEmpty();
                    emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
                    rv.setVisibility(empty ? View.GONE : View.VISIBLE);
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Models.AdminUser>> call, @NonNull Throwable t) {
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

    private String roleLabel(String role) {
        int res;
        switch (role == null ? "" : role) {
            case "ADMIN": res = R.string.role_ADMIN; break;
            case "DOCTOR": res = R.string.role_DOCTOR; break;
            default: res = R.string.role_PATIENT;
        }
        return getString(res);
    }

    private int roleBg(String role) {
        switch (role == null ? "" : role) {
            case "ADMIN": return R.color.statusCompletedBg;
            case "DOCTOR": return R.color.statusConfirmedBg;
            default: return R.color.statusPendingBg;
        }
    }

    private int roleText(String role) {
        switch (role == null ? "" : role) {
            case "ADMIN": return R.color.statusCompletedText;
            case "DOCTOR": return R.color.statusConfirmedText;
            default: return R.color.statusPendingText;
        }
    }

    private class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.VH> {

        private final List<Models.AdminUser> items = new ArrayList<>();

        void submit(List<Models.AdminUser> users) {
            items.clear();
            if (users != null) items.addAll(users);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_user, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Models.AdminUser u = items.get(position);
            holder.avatar.setText(Avatar.initials(u.fullName));
            holder.avatar.setBackgroundTintList(Avatar.tintFor(holder.itemView.getContext(), u.fullName));
            holder.avatar.setTextColor(Avatar.textColor(holder.itemView.getContext()));
            holder.name.setText(u.fullName);
            holder.email.setText(u.email);
            holder.role.setText(roleLabel(u.role));
            holder.role.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), roleBg(u.role))));
            holder.role.setTextColor(androidx.core.content.ContextCompat.getColor(
                    holder.itemView.getContext(), roleText(u.role)));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView avatar, name, email, role;

            VH(@NonNull View itemView) {
                super(itemView);
                avatar = itemView.findViewById(R.id.avatar);
                name = itemView.findViewById(R.id.user_name);
                email = itemView.findViewById(R.id.user_email);
                role = itemView.findViewById(R.id.user_role);
            }
        }
    }
}
