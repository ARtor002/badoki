package ir.artor.badoki.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public class DoctorRequest {

    @NotBlank(message = "نام پزشک الزامی است")
    @Size(max = 120)
    private String fullName;

    @NotBlank(message = "تخصص الزامی است")
    @Size(max = 80)
    private String specialty;

    @NotBlank(message = "شهر الزامی است")
    @Size(max = 60)
    private String city;

    @Size(max = 120)
    private String hospitalName;

    @Size(max = 300)
    private String address;

    @Size(max = 1000)
    private String bio;

    @NotNull(message = "هزینه ویزیت الزامی است")
    @Positive(message = "هزینه ویزیت باید مثبت باشد")
    private Long visitPrice;

    @Min(value = 0, message = "سال سابقه نمی‌تواند منفی باشد")
    private int experienceYears;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private double rating = 4.5;

    @Min(0)
    private int reviewCount = 0;

    @NotEmpty(message = "حداقل یک روز کاری انتخاب کنید")
    private List<String> availableDays;

    @Min(value = 0, message = "ساعت شروع نامعتبر است")
    @Max(value = 23, message = "ساعت شروع نامعتبر است")
    private int startHour = 9;

    @Min(value = 1, message = "ساعت پایان نامعتبر است")
    @Max(value = 24, message = "ساعت پایان نامعتبر است")
    private int endHour = 17;

    @Min(value = 15, message = "مدت هر نوبت حداقل ۱۵ دقیقه است")
    @Max(value = 120, message = "مدت هر نوبت حداکثر ۱۲۰ دقیقه است")
    private int slotMinutes = 30;

    /** ایمیل حساب کاربری پزشک — اگر داده شود، حساب با نقش DOCTOR ساخته/متصل می‌شود */
    @Email(message = "فرمت ایمیل صحیح نیست")
    private String email;

    /** رمز حساب پزشک (پیش‌فرض: 123456) */
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Long getVisitPrice() { return visitPrice; }
    public void setVisitPrice(Long visitPrice) { this.visitPrice = visitPrice; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public List<String> getAvailableDays() { return availableDays; }
    public void setAvailableDays(List<String> availableDays) { this.availableDays = availableDays; }

    public int getStartHour() { return startHour; }
    public void setStartHour(int startHour) { this.startHour = startHour; }

    public int getEndHour() { return endHour; }
    public void setEndHour(int endHour) { this.endHour = endHour; }

    public int getSlotMinutes() { return slotMinutes; }
    public void setSlotMinutes(int slotMinutes) { this.slotMinutes = slotMinutes; }
}
