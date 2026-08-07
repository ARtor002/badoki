package ir.artor.badoki.dto;

import jakarta.validation.constraints.*;

import java.util.List;

/** به‌روزرسانی پروفایل توسط خود پزشک — نام، تخصص و شهر فقط توسط ادمین قابل تغییر است */
public class DoctorProfileUpdateRequest {

    @Size(max = 120)
    private String hospitalName;

    @Size(max = 300)
    private String address;

    @Size(max = 1000)
    private String bio;

    @NotNull(message = "هزینه ویزیت الزامی است")
    @Positive(message = "هزینه ویزیت باید مثبت باشد")
    private Long visitPrice;

    @Min(value = 0, message = "ساعت شروع نامعتبر است")
    @Max(value = 23, message = "ساعت شروع نامعتبر است")
    private Integer startHour;

    @Min(value = 1, message = "ساعت پایان نامعتبر است")
    @Max(value = 24, message = "ساعت پایان نامعتبر است")
    private Integer endHour;

    @Min(value = 15, message = "مدت هر نوبت حداقل ۱۵ دقیقه است")
    @Max(value = 120, message = "مدت هر نوبت حداکثر ۱۲۰ دقیقه است")
    private Integer slotMinutes;

    @NotEmpty(message = "حداقل یک روز کاری انتخاب کنید")
    private List<String> availableDays;

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Long getVisitPrice() { return visitPrice; }
    public void setVisitPrice(Long visitPrice) { this.visitPrice = visitPrice; }

    public Integer getStartHour() { return startHour; }
    public void setStartHour(Integer startHour) { this.startHour = startHour; }

    public Integer getEndHour() { return endHour; }
    public void setEndHour(Integer endHour) { this.endHour = endHour; }

    public Integer getSlotMinutes() { return slotMinutes; }
    public void setSlotMinutes(Integer slotMinutes) { this.slotMinutes = slotMinutes; }

    public List<String> getAvailableDays() { return availableDays; }
    public void setAvailableDays(List<String> availableDays) { this.availableDays = availableDays; }
}
