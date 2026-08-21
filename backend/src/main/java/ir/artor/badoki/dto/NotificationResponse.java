package ir.artor.badoki.dto;

import ir.artor.badoki.model.Notification;

public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean read;
    private String createdAt;

    public static NotificationResponse from(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.id = n.getId();
        r.title = n.getTitle();
        r.message = n.getMessage();
        r.type = n.getType();
        r.read = n.isRead();
        r.createdAt = n.getCreatedAt() != null ? n.getCreatedAt().toString() : null;
        return r;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public boolean isRead() {
        return read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public NotificationResponse() {
    }
}
