package ir.artor.badoki.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AppointmentRequest {

    @NotNull(message = "شناسه پزشک الزامی است")
    private Long doctorId;

    /** تاریخ به فرمت ISO (مثلاً 2026-08-10) */
    @NotBlank(message = "تاریخ الزامی است")
    private String date;

    /** ساعت به فرمت HH:mm (مثلاً 09:30) */
    @NotBlank(message = "ساعت الزامی است")
    private String time;

    @Size(max = 500, message = "توضیحات حداکثر ۵۰۰ کاراکتر می‌تواند باشد")
    private String notes;

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
