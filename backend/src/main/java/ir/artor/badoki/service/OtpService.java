package ir.artor.badoki.service;

import ir.artor.badoki.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * مدیریت کدهای تأیید یکبارمصرف (OTP) در حافظه:
 * - کد ۶ رقمی با انقضای ۵ دقیقه
 * - حداکثر ۵ تلاش اشتباه
 * - فاصله اجباری بین دو ارسال (جلوگیری از اسپم)
 * - پاک‌سازی دوره‌ای کدهای منقضی
 */
@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_ATTEMPTS = 5;

    private final MailService mailService;
    private final int expiryMinutes;
    private final int cooldownSeconds;
    private final boolean devMode;

    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();

    public OtpService(MailService mailService,
                      @Value("${app.otp.expiry-minutes:5}") int expiryMinutes,
                      @Value("${app.otp.resend-cooldown-seconds:60}") int cooldownSeconds,
                      @Value("${app.otp.dev-mode:true}") boolean devMode) {
        this.mailService = mailService;
        this.expiryMinutes = expiryMinutes;
        this.cooldownSeconds = cooldownSeconds;
        this.devMode = devMode;
    }

    /** تولید و ارسال کد؛ خروجی: خود کد (فقط در حالت توسعه استفاده می‌شود) */
    public String send(String email, OtpPurpose purpose) {
        String key = key(email, purpose);
        OtpEntry existing = store.get(key);
        if (existing != null && existing.lastSentAt != null) {
            long passed = Duration.between(existing.lastSentAt, Instant.now()).getSeconds();
            if (passed < cooldownSeconds) {
                long wait = cooldownSeconds - passed;
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                        "کد قبلی هنوز معتبر است؛ " + wait + " ثانیه دیگر دوباره تلاش کنید");
            }
        }
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        store.put(key, new OtpEntry(code, purpose,
                Instant.now().plus(Duration.ofMinutes(expiryMinutes)), Instant.now(), 0));
        mailService.sendOtp(email, code, purpose);
        return code;
    }

    /** بررسی صحت کد؛ در صورت موفقیت، کد از حافظه حذف می‌شود */
    public void verify(String email, OtpPurpose purpose, String code) {
        String key = key(email, purpose);
        OtpEntry entry = store.get(key);
        if (entry == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "کد تأیید منقضی شده یا نامعتبر است؛ دوباره درخواست دهید");
        }
        if (entry.attempts >= MAX_ATTEMPTS) {
            store.remove(key);
            throw new ApiException(HttpStatus.BAD_REQUEST, "تعداد تلاش‌های ناموفق بیش از حد مجاز بود؛ دوباره درخواست دهید");
        }
        if (Instant.now().isAfter(entry.expiresAt)) {
            store.remove(key);
            throw new ApiException(HttpStatus.BAD_REQUEST, "کد تأیید منقضی شده است؛ دوباره درخواست دهید");
        }
        if (!entry.code.equals(code)) {
            entry.attempts++;
            throw new ApiException(HttpStatus.BAD_REQUEST, "کد تأیید اشتباه است");
        }
        store.remove(key);
    }

    /** آیا باید کد را در پاسخ API برگردانیم؟ (فقط برای توسعه/دمو) */
    public boolean isDevMode() {
        return devMode || !mailService.isConfigured();
    }

    /** کد را در پاسخ برگردان (فقط در حالت توسعه) */
    public String devCodeOrNull(String code) {
        return isDevMode() ? code : null;
    }

    private String key(String email, OtpPurpose purpose) {
        return email.trim().toLowerCase() + ":" + purpose.name();
    }

    /** پاک‌سازی دوره‌ای کدهای منقضی */
    @Scheduled(fixedDelay = 600_000)
    public void purgeExpired() {
        Instant now = Instant.now();
        store.entrySet().removeIf(e -> e.getValue().expiresAt.isBefore(now));
    }

    private static class OtpEntry {
        final String code;
        final OtpPurpose purpose;
        final Instant expiresAt;
        final Instant lastSentAt;
        int attempts;

        OtpEntry(String code, OtpPurpose purpose, Instant expiresAt, Instant lastSentAt, int attempts) {
            this.code = code;
            this.purpose = purpose;
            this.expiresAt = expiresAt;
            this.lastSentAt = lastSentAt;
            this.attempts = attempts;
        }
    }
}
