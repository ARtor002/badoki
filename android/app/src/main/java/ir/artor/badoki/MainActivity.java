package ir.artor.badoki;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import ir.artor.badoki.ui.AdminDashboardFragment;
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
