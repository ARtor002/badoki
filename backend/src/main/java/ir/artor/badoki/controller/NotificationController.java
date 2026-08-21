package ir.artor.badoki.controller;

import ir.artor.badoki.dto.NotificationResponse;
import ir.artor.badoki.dto.UnreadCountResponse;
import ir.artor.badoki.model.User;
import ir.artor.badoki.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** مرکز اطلاع‌رسانی درون‌برنامه‌ای */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> list(@AuthenticationPrincipal User user) {
        return notificationService.listForUser(user.getId());
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount(@AuthenticationPrincipal User user) {
        return new UnreadCountResponse(notificationService.unreadCount(user.getId()));
    }

    @PutMapping("/{id}/read")
    public void markRead(@AuthenticationPrincipal User user, @PathVariable Long id) {
        notificationService.markRead(user.getId(), id);
    }

    @PutMapping("/read-all")
    public void markAllRead(@AuthenticationPrincipal User user) {
        notificationService.markAllRead(user.getId());
    }
}
