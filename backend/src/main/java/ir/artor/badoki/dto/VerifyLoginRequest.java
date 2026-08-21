package ir.artor.badoki.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyLoginRequest {

    @NotBlank(message = "ایمیل الزامی است")
    @Email(message = "فرمت ایمیل صحیح نیست")
    private String email;

    @NotBlank(message = "کد تأیید الزامی است")
    @Pattern(regexp = "^\\d{6}$", message = "کد تأیید باید ۶ رقم باشد")
    private String otp;

    public VerifyLoginRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
