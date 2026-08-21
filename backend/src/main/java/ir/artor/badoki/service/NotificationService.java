package ir.artor.badoki.service;

import ir.artor.badoki.ApiException;
import ir.artor.badoki.dto.NotificationResponse;
import ir.artor.badoki.model.Notification;
import ir.artor.badoki.model.User;
import ir.artor.badoki.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** ساخت و مدیریت اعلان‌های درون‌برنامه‌ای */
@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    /** ایجاد اعلان برای یک کاربر */
    @Transactional
    public void notifyUser(Long userId, String title, String message, String type) {
        Notification n = new Notification();
        User u = new User();
        u.setId(userId);
        n.setUser(u);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        repository.save(n);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listForUser(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(100)
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return repository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification n = repository.findById(notificationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "اعلان پیدا نشد"));
        if (!n.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "دسترسی به این اعلان ندارید");
        }
        n.setRead(true);
        repository.save(n);
    }

    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> unread = repository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        repository.saveAll(unread);
    }
}
