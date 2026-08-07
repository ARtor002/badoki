package ir.artor.badoki.dto;

import jakarta.validation.constraints.NotBlank;

/** تغییر وضعیت نوبت توسط پزشک: CONFIRMED | COMPLETED | CANCELED */
public class StatusUpdateRequest {

    @NotBlank(message = "وضعیت جدید الزامی است")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
