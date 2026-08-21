package ir.artor.badoki.dto;

public class AuthResponse {

    private String token;
    private UserResponse user;

    /** درست اگر ورود نیاز به مرحله دوم (کد تأیید) داشته باشد */
    private boolean requiresOtp;

    /** ایمیل در انتظار تأیید */
    private String email;

    /** پیام راهنما (مثلاً «کد تأیید ارسال شد») */
    private String message;

    /** کد آزمایشی — فقط در حالت توسعه */
    private String devOtp;

    public AuthResponse() {}

    public AuthResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public boolean isRequiresOtp() {
        return requiresOtp;
    }

    public void setRequiresOtp(boolean requiresOtp) {
        this.requiresOtp = requiresOtp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDevOtp() {
        return devOtp;
    }

    public void setDevOtp(String devOtp) {
        this.devOtp = devOtp;
    }

    public String getToken() { return token; }
    public UserResponse getUser() { return user; }
}
