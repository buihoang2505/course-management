package com.example.course.coursemanagement.scheduled;

import com.example.course.coursemanagement.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationCleanupJob {

    private static final int KEEP_DAYS = 30;

    private final NotificationRepository notifRepo;

    // Chạy mỗi đêm lúc 2:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(KEEP_DAYS);
        long count = notifRepo.countOlderThan(cutoff);
        if (count == 0) {
            log.info("[NotificationCleanup] Không có thông báo cũ cần xóa.");
            return;
        }
        int deleted = notifRepo.deleteOlderThan(cutoff);
        log.info("[NotificationCleanup] Đã xóa {} thông báo cũ hơn {} ngày.", deleted, KEEP_DAYS);
    }
}