package ir.artor.badoki.util;

import android.content.Context;
import android.content.SharedPreferences;

import ir.artor.badoki.api.Models;

/** نگهداری امن توکن و اطلاعات کاربر در SharedPreferences */
public class SessionManager {

    private static final String PREFS = "drsalam_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ID = "user_id";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_PHONE = "user_phone";
    private static final String KEY_ROLE = "user_role";

    private static SharedPreferences sp;

    public static void init(Context context) {
        sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void saveLogin(Models.AuthResponse response) {
        sp.edit()
                .putString(KEY_TOKEN, response.token)
                .putLong(KEY_ID, response.user.id)
                .putString(KEY_NAME, response.user.fullName)
                .putString(KEY_EMAIL, response.user.email)
                .putString(KEY_PHONE, response.user.phone == null ? "" : response.user.phone)
                .putString(KEY_ROLE, response.user.role == null ? "PATIENT" : response.user.role)
                .apply();
    }

    public static String token() {
        return sp.getString(KEY_TOKEN, null);
    }

    public static long userId() {
        return sp.getLong(KEY_ID, -1);
    }

    public static String name() {
        return sp.getString(KEY_NAME, "");
    }

    public static String email() {
        return sp.getString(KEY_EMAIL, "");
    }

    public static String phone() {
        return sp.getString(KEY_PHONE, "");
    }

    public static void updateProfile(String name, String phone) {
        sp.edit()
                .putString(KEY_NAME, name)
                .putString(KEY_PHONE, phone == null ? "" : phone)
                .apply();
    }

    public static boolean isLoggedIn() {
        return token() != null;
    }

    public static String role() {
        return sp.getString(KEY_ROLE, "PATIENT");
    }

    public static boolean isDoctor() {
        return "DOCTOR".equals(role());
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(role());
    }

    public static void logout() {
        sp.edit().clear().apply();
    }
}
