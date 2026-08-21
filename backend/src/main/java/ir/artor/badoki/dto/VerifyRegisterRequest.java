package ir.artor.badoki.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VerifyRegisterRequest {

    @NotBlank(message = "ایمیل الزامی است")
    @Email(message = "فرمت ایمیل صحیح نیست")
    private String email;

    @NotBlank(message = "کد تأیید الزامی است")
    @Pattern(regexp = "^\\d{6}$", message = "کد تأیید باید ۶ رقم باشد")
    private String otp;

    @NotBlank(message = "نام و نام خانوادگی الزامی است")
    @Size(max = 100)
    private String fullName;

    @Pattern(regexp = "^09\\d{9}$", message = "شماره موبایل باید با ۰۹ شروع شود و ۱۱ رقم باشد")
    private String phone;

    @NotBlank(message = "رمز عبور الزامی است")
    @Size(min = 6, message = "رمز عبور باید حداقل ۶ کاراکتر باشد")
    private String password;

    public VerifyRegisterRequest() {
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
