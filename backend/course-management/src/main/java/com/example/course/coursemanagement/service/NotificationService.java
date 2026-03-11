package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.entity.Notification;
import com.example.course.coursemanagement.entity.User;
import com.example.course.coursemanagement.repository.NotificationRepository;
import com.example.course.coursemanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notifRepo;
    private final UserRepository         userRepo;

    public Notification create(Long userId, Notification.Type type,
                               String title, String message, String link) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setLink(link);
        return notifRepo.save(n);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        return notifRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notifRepo.countByUserIdAndIsReadFalse(userId);
    }

    public void markRead(Long notifId) {
        notifRepo.findById(notifId).ifPresent(n -> {
            n.setRead(true);
            notifRepo.save(n);
        });
    }

    public void markAllRead(Long userId) {
        notifRepo.markAllReadByUserId(userId);
    }

    // ── Xóa tất cả thông báo của user ──
    public void clearAll(Long userId) {
        notifRepo.deleteByUserId(userId);
    }

    public void notifyGrade(Long userId, String courseName, double score) {
        create(userId, Notification.Type.GRADE,
                "Điểm mới!",
                String.format("Bạn đã được nhập điểm %.1f cho khóa học \"%s\"", score, courseName),
                "/grades");
    }

    public void notifyEnrollment(Long userId, String courseName, Long courseId) {
        create(userId, Notification.Type.ENROLLMENT,
                "Đăng ký thành công!",
                String.format("Bạn đã đăng ký khóa học \"%s\" thành công", courseName),
                "/courses/" + courseId);
    }

    public void notifyCompletion(Long userId, String courseName) {
        create(userId, Notification.Type.COMPLETION,
                "🎉 Hoàn thành khóa học!",
                String.format("Chúc mừng! Bạn đã hoàn thành khóa học \"%s\"", courseName),
                "/my-courses");
    }
    // Thêm method này vào NotificationService.java (đã có sẵn)

    public void notifyCertificate(Long userId, String courseName, String certCode) {
        create(userId, Notification.Type.COMPLETION,
                "🏆 Chứng chỉ mới!",
                String.format("Chúc mừng! Bạn đã nhận chứng chỉ hoàn thành khóa học \"%s\"", courseName),
                "/certificates");
    }
}