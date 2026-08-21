package ir.artor.badoki.controller;

import ir.artor.badoki.dto.*;
import ir.artor.badoki.model.User;
import ir.artor.badoki.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ---------- ثبت‌نام دومرحله‌ای ----------

    /** گام ۱: ارسال کد تأیید به ایمیل */
    @PostMapping("/send-register-otp")
    public OtpResponse sendRegisterOtp(@Valid @RequestBody EmailRequest request) {
        return authService.sendRegisterOtp(request.getEmail());
    }

    /** گام ۲: تأیید کد و ساخت حساب */
    @PostMapping("/verify-register")
    public AuthResponse verifyRegister(@Valid @RequestBody VerifyRegisterRequest request) {
        return authService.verifyRegister(request);
    }

    // ---------- ورود دومرحله‌ای ----------

    /** گام ۱: بررسی رمز عبور (در صورت 2FA، کد ارسال می‌شود) */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** گام ۲: تأیید کد و دریافت توکن */
    @PostMapping("/verify-login")
    public AuthResponse verifyLogin(@Valid @RequestBody VerifyLoginRequest request) {
        return authService.verifyLogin(request);
    }

    // ---------- فراموشی رمز عبور ----------

    @PostMapping("/forgot-password")
    public OtpResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request.getEmail());
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
    }

    // ---------- تنظیم 2FA (پروفایل) ----------

    @PutMapping("/two-factor")
    public void setTwoFactor(@AuthenticationPrincipal User user,
                             @RequestBody Map<String, Boolean> body) {
        authService.setTwoFactor(user, Boolean.TRUE.equals(body.get("enabled")));
    }
}
