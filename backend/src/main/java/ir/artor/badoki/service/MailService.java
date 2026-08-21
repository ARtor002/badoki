package ir.artor.badoki.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/** ارسال ایمیل از طریق Gmail SMTP (با App Password) — اگر پیکربندی نشده باشد، کد در لاگ چاپ می‌شود */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final String from;

    public MailService(JavaMailSender mailSender,
                       @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender;
        this.from = from == null ? "" : from;
    }

    public boolean isConfigured() {
        return from != null && !from.isBlank();
    }

    public void sendOtp(String to, String code, OtpPurpose purpose) {
        String purposeLabel = switch (purpose) {
            case REGISTER -> "ثبت‌نام در سامانه بادُکی";
            case LOGIN -> "ورود به سامانه بادُکی";
            case RESET -> "بازیابی رمز عبور بادُکی";
        };
        String subject = "کد تأیید بادُکی";
        String body = "سلام،\n\n"
                + "کد تأیید شما برای " + purposeLabel + ":\n\n"
                + "    " + code + "\n\n"
                + "این کد تا ۵ دقیقه معتبر است. اگر شما این درخواست را نداده‌اید، این ایمیل را نادیده بگیرید.\n\n"
                + "— سامانه بادُکی";

        if (!isConfigured()) {
            log.warn("SMTP پیکربندی نشده است — کد OTP برای {} : {}", to, code);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            mailSender.send(message);
            log.info("ایمیل کد تأیید به {} ارسال شد", to);
        } catch (Exception e) {
            log.error("خطا در ارسال ایمیل به {}: {}", to, e.getMessage());
        }
    }
}
