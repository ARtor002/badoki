package ir.artor.badoki.dto;

import ir.artor.badoki.model.Review;

public class ReviewResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private int rating;
    private String comment;
    private String createdAt;

    public static ReviewResponse from(Review r) {
        ReviewResponse resp = new ReviewResponse();
        resp.id = r.getId();
        resp.patientId = r.getPatient().getId();
        resp.patientName = r.getPatient().getFullName();
        resp.rating = r.getRating();
        resp.comment = r.getComment();
        resp.createdAt = r.getCreatedAt() != null ? r.getCreatedAt().toString() : null;
        return resp;
    }

    public ReviewResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
