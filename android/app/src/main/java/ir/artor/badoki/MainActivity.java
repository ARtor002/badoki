package ir.artor.badoki;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.navigation.NavigationView;

import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.Models;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import ir.artor.badoki.ui.AdminDashboardFragment;
import ir.artor.badoki.ui.EducationFragment;
import ir.artor.badoki.ui.NotificationsFragment;
import ir.artor.badoki.ui.AdminDoctorsFragment;
import ir.artor.badoki.ui.AdminUsersFragment;
import ir.artor.badoki.ui.AppointmentsFragment;
import ir.artor.badoki.ui.DashboardFragment;
import ir.artor.badoki.ui.DoctorAppointmentsFragment;
import ir.artor.badoki.ui.DoctorDashboardFragment;
import ir.artor.badoki.ui.DoctorProfileFragment;
import ir.artor.badoki.ui.DoctorsFragment;
import ir.artor.badoki.ui.ProfileFragment;
import ir.artor.badoki.util.Avatar;
import ir.artor.badoki.util.SessionManager;

/**
 * قاب اصلی اپ: کشوی کناری (سایدبار) + نوار ابزار + کانتینر فرگمنت‌ها
 * منو بر اساس نقش کاربر (PATIENT | DOCTOR | ADMIN) نمایش داده می‌شود.
 */
public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private Toolbar toolbar;
    private BadgeDrawable notifBadge;
    private final android.os.Handler badgePoller = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable badgeTask = new Runnable() {
        @Override
        public void run() {
            refreshUnreadBadge();
            badgePoller.postDelayed(this, 30_000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.nav_dashboard, R.string.nav_dashboard);
        toggle.getDrawerArrowDrawable().setColor(
                getColor(R.color.onSurface));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupMenuByRole();

        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                openHome(R.string.nav_dashboard);
            } else if (id == R.id.nav_doctors) {
                open(new DoctorsFragment(), getString(R.string.nav_doctors), false);
            } else if (id == R.id.nav_appointments) {
                open(new AppointmentsFragment(), getString(R.string.nav_appointments), false);
            } else if (id == R.id.nav_doctor_appointments) {
                open(new DoctorAppointmentsFragment(), getString(R.string.nav_doctor_appointments), false);
            } else if (id == R.id.nav_admin_doctors) {
                open(new AdminDoctorsFragment(), getString(R.string.nav_admin_doctors), false);
            } else if (id == R.id.nav_admin_users) {
                open(new AdminUsersFragment(), getString(R.string.nav_admin_users), false);
            } else if (id == R.id.nav_notifications) {
                open(new NotificationsFragment(), getString(R.string.nav_notifications), false);
            } else if (id == R.id.nav_education) {
                open(new EducationFragment(), getString(R.string.nav_education), false);
            } else if (id == R.id.nav_profile) {
                openProfile();
            }
            return true;
        });

        // صفحه شروع بر اساس نقش
        if (savedInstanceState == null) {
            openHome(R.string.nav_dashboard);
        }
        updateDrawerHeader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDrawerHeader();
        if (SessionManager.isLoggedIn()) {
            refreshUnreadBadge();
            badgePoller.removeCallbacks(badgeTask);
            badgePoller.postDelayed(badgeTask, 30_000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        badgePoller.removeCallbacks(badgeTask);
    }

    /** نمایش آیتم‌های منو بر اساس نقش */
    private void setupMenuByRole() {
        Menu menu = navView.getMenu();
        boolean patient = SessionManager.role().equals("PATIENT");
        boolean doctor = SessionManager.isDoctor();
        boolean admin = SessionManager.isAdmin();
        menu.findItem(R.id.nav_doctors).setVisible(patient);
        menu.findItem(R.id.nav_appointments).setVisible(patient);
        menu.findItem(R.id.nav_doctor_appointments).setVisible(doctor);
        menu.findItem(R.id.nav_admin_doctors).setVisible(admin);
        menu.findItem(R.id.nav_admin_users).setVisible(admin);
        // اطلاع‌رسانی و آموزش برای همه نقش‌ها
        menu.findItem(R.id.nav_notifications).setVisible(true);
        menu.findItem(R.id.nav_education).setVisible(true);

        // نشانگر (Badge) اعلان‌های ناخوانده — بعد از رندر آیتم منو متصل می‌شود
        navView.post(() -> {
            View itemView = navView.findViewById(R.id.nav_notifications);
            if (itemView != null) {
                notifBadge = BadgeDrawable.create(MainActivity.this);
                notifBadge.setBadgeTextColor(getColor(R.color.white));
                notifBadge.setBackgroundColor(getColor(R.color.error));
                notifBadge.setMaxCharacterCount(3);
                notifBadge.setVisible(false);
                BadgeUtils.attachBadgeDrawable(notifBadge, itemView);
            }
        });
    }

    /** دریافت و نمایش تعداد اعلان‌های ناخوانده */
    public void refreshUnreadBadge() {
        if (notifBadge == null || !SessionManager.isLoggedIn()) return;
        BadokiApp.api().unreadCount().enqueue(new Callback<Models.UnreadCount>() {
            @Override
            public void onResponse(@NonNull Call<Models.UnreadCount> call,
                                   @NonNull Response<Models.UnreadCount> response) {
                if (response.isSuccessful() && response.body() != null) {
                    long count = response.body().count;
                    notifBadge.setVisible(count > 0);
                    notifBadge.setNumber((int) count);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Models.UnreadCount> call, @NonNull Throwable t) {
                if (ApiClient.isUnauthorized(t)) {
                    ApiClient.handleUnauthorized(MainActivity.this);
                }
            }
        });
    }

    /** صفحه داشبورد متناسب با نقش */
    public void openHome(int titleRes) {
        Fragment fragment;
        if (SessionManager.isDoctor()) {
            fragment = new DoctorDashboardFragment();
        } else if (SessionManager.isAdmin()) {
            fragment = new AdminDashboardFragment();
        } else {
            fragment = new DashboardFragment();
        }
        open(fragment, getString(titleRes), false);
    }

    public void openProfile() {
        if (SessionManager.isDoctor()) {
            open(new DoctorProfileFragment(), getString(R.string.doctor_profile_title), false);
        } else {
            open(new ProfileFragment(), getString(R.string.nav_profile), false);
        }
    }

    /** به‌روزرسانی نام و ایمیل در هدر کشو */
    public void updateDrawerHeader() {
        View header = navView.getHeaderView(0);
        if (header == null) return;
        TextView name = header.findViewById(R.id.header_name);
        TextView email = header.findViewById(R.id.header_email);
        TextView avatar = header.findViewById(R.id.header_avatar);
        String fullName = SessionManager.name();
        name.setText(fullName);
        email.setText(SessionManager.email());
        avatar.setText(Avatar.initials(fullName));
        avatar.setBackgroundTintList(Avatar.tintFor(this, fullName));
        avatar.setTextColor(Avatar.textColor(this));
    }

    /** باز کردن فرگمنت با عنوان و بک‌استک */
    public void open(Fragment fragment, String title, boolean addToBack) {
        androidx.fragment.app.FragmentTransaction tx =
                getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(
                android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out);
        tx.replace(R.id.main_container, fragment);
        if (addToBack) {
            tx.addToBackStack(null);
        }
        tx.commit();
        if (toolbar != null) toolbar.setTitle(title);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
