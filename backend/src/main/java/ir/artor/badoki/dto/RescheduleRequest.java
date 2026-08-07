package ir.artor.badoki.dto;

import jakarta.validation.constraints.NotBlank;

public class RescheduleRequest {

    @NotBlank(message = "تاریخ جدید الزامی است")
    private String date;

    @NotBlank(message = "ساعت جدید الزامی است")
    private String time;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
