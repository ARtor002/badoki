package ir.artor.badoki.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest {

    @NotBlank(message = "نام و نام خانوادگی الزامی است")
    @Size(max = 100, message = "نام نباید بیشتر از ۱۰۰ کاراکتر باشد")
    private String fullName;

    private String phone;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
