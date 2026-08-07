package ir.artor.badoki;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import ir.artor.badoki.api.ApiClient;
import ir.artor.badoki.api.ApiService;
import ir.artor.badoki.util.SessionManager;

public class BadokiApp extends Application {

    private static BadokiApp instance;
    private static ApiService api;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SessionManager.init(this);
        api = ApiClient.create();

        // کل اپ فارسی و راست‌به‌چپ باشد، حتی اگر زبان دستگاه فارسی نباشد
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("fa"));
    }

    public static BadokiApp get() {
        return instance;
    }

    public static ApiService api() {
        return api;
    }
}
