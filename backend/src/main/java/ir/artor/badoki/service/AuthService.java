package ir.artor.badoki.service;

import ir.artor.badoki.ApiException;
import ir.artor.badoki.dto.*;
import ir.artor.badoki.model.Role;
import ir.artor.badoki.model.User;
import ir.artor.badoki.repository.UserRepository;
import ir.artor.badoki.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
    }

    // ---------- ثبت‌نام دومرحله‌ای ----------

    /** گام ۱: ارسال کد تأیید به ایمیل */
    public OtpResponse sendRegisterOtp(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        OtpPurpose purpose = userRepository.existsByEmail(email)
                ? OtpPurpose.LOGIN      // ایمیل موجود → کد ورود (برای ارسال مجدد در ورود)
                : OtpPurpose.REGISTER;  // ایمیل تازه → کد ثبتنام
        String code = otpService.send(email, purpose);
        return new OtpResponse("کد تأیید به ایمیل شما ارسال شد", otpService.devCodeOrNull(code));
    }

    /** گام ۲: تأیید کد و ساخت حساب */
    @Transactional
    public AuthResponse verifyRegister(VerifyRegisterRequest req) {
        String email = normalizeEmail(req.getEmail());
        otpService.verify(email, OtpPurpose.REGISTER, req.getOtp());
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "این ایمیل قبلاً ثبت شده است. وارد شوید.");
        }
        User user = new User();
        user.setFullName(req.getFullName().trim());
        user.setEmail(email);
        user.setPhone(req.getPhone());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(Role.PATIENT);
        user.setTwoFactorEnabled(true);
        user.setCreatedAt(Instant.now());
        userRepository.save(user);
        return toAuthResponse(user);
    }

    // ---------- ورود دومرحله‌ای ----------

    /** گام ۱: بررسی رمز عبور — در صورت فعال بودن 2FA، کد ارسال می‌شود */
    public AuthResponse login(LoginRequest req) {
        User user = findByEmailOrThrow(req.getEmail());
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "ایمیل یا رمز عبور اشتباه است");
        }
        if (user.isTwoFactorEnabled()) {
            String code = otpService.send(user.getEmail(), OtpPurpose.LOGIN);
            AuthResponse resp = new AuthResponse();
            resp.setRequiresOtp(true);
            resp.setEmail(user.getEmail());
            resp.setMessage("کد تأیید به ایمیل شما ارسال شد");
            resp.setDevOtp(otpService.devCodeOrNull(code));
            return resp;
        }
        return toAuthResponse(user);
    }

    /** گام ۲: تأیید کد و دریافت توکن */
    public AuthResponse verifyLogin(VerifyLoginRequest req) {
        String email = normalizeEmail(req.getEmail());
        otpService.verify(email, OtpPurpose.LOGIN, req.getOtp());
        return toAuthResponse(findByEmailOrThrow(email));
    }

    // ---------- فراموشی رمز عبور ----------

    /** ارسال کد بازیابی */
    public OtpResponse forgotPassword(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "کاربری با این ایمیل یافت نشد"));
        String code = otpService.send(user.getEmail(), OtpPurpose.RESET);
        return new OtpResponse("کد بازیابی به ایمیل شما ارسال شد", otpService.devCodeOrNull(code));
    }

    /** بازنشانی رمز عبور با کد */
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        String email = normalizeEmail(req.getEmail());
        otpService.verify(email, OtpPurpose.RESET, req.getOtp());
        User user = findByEmailOrThrow(email);
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    // ---------- تنظیمات 2FA ----------

    @Transactional
    public void setTwoFactor(User user, boolean enabled) {
        user.setTwoFactorEnabled(enabled);
        userRepository.save(user);
    }

    // ---------- ابزارها ----------

    private User findByEmailOrThrow(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "ایمیل یا رمز عبور اشتباه است"));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        UserResponse u = new UserResponse(
                user.getId(), user.getFullName(), user.getEmail(),
                user.getPhone(), user.getRole(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return new AuthResponse(token, u);
    }
}
