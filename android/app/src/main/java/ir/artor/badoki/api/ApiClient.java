package ir.artor.badoki.api;

import android.content.Context;
import android.content.Intent;

import ir.artor.badoki.AuthActivity;
import ir.artor.badoki.BuildConfig;
import ir.artor.badoki.util.SessionManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    /**
     * آدرس سرور:
     *  - ایمولاتور اندروید: 10.0.2.2 به کامپیوتر شما اشاره می‌کند
     *  - گوشی واقعی: آدرس LAN کامپیوتر (مثلاً http://192.168.1.5:8080/)
     *  یا با دستور adb reverse tcp:8080 tcp:8080 و آدرس http://localhost:8080/
     */
    public static final String BASE_URL = "http://10.250.141.61:8080/";

    public static ApiService create() {
        Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            String token = SessionManager.token();
            if (token != null) {
                original = original.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
            }
            return chain.proceed(original);
        };

        // نمایش کامل درخواست/پاسخ در Logcat (فقط در نسخه debug) — برای عیب‌یابی خطاهای HTTP
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor(authInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(ApiService.class);
    }

    /** استخراج پیام خطای فارسی از پاسخ سرور */
    public static String errorMessage(Throwable t) {
        if (t instanceof HttpException) {
            HttpException he = (HttpException) t;
            try {
                String body = he.response().errorBody() != null
                        ? he.response().errorBody().string() : null;
                if (body != null) {
                    JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
                    if (obj.has("message")) {
                        return obj.get("message").getAsString();
                    }
                }
            } catch (Exception ignored) {
            }
            return "خطای سرور (کد " + he.code() + ")";
        }
        if (t instanceof IOException) {
            return "خطا در ارتباط با سرور. اتصال اینترنت را بررسی کنید";
        }
        return "مشکلی پیش آمد. دوباره تلاش کنید";
    }

    public static boolean isUnauthorized(Throwable t) {
        return t instanceof HttpException && ((HttpException) t).code() == 401;
    }

    /** در صورت انقضای توکن، به صفحه ورود برمی‌گرداند */
    public static void handleUnauthorized(Context ctx) {
        if (!SessionManager.isLoggedIn()) return;
        SessionManager.logout();
        Intent intent = new Intent(ctx, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ctx.startActivity(intent);
    }
}
