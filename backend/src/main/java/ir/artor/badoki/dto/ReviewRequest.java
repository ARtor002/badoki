package ir.artor.badoki.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** ثبت نظر و امتیاز برای پزشک */
public class ReviewRequest {

    @NotNull(message = "شناسه پزشک الزامی است")
    private Long doctorId;

    @NotNull(message = "امتیاز الزامی است")
    @Min(value = 1, message = "امتیاز باید بین ۱ تا ۵ باشد")
    @Max(value = 5, message = "امتیاز باید بین ۱ تا ۵ باشد")
    private Integer rating;

    @Size(max = 500, message = "نظر حداکثر ۵۰۰ کاراکتر می‌تواند باشد")
    private String comment;

    public ReviewRequest() {
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
