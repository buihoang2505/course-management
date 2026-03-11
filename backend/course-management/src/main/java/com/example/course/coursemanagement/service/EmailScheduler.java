package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.entity.EmailQueue;
import com.example.course.coursemanagement.entity.Enrollment;
import com.example.course.coursemanagement.repository.EmailQueueRepository;
import com.example.course.coursemanagement.repository.EnrollmentRepository;
import com.example.course.coursemanagement.repository.UserRepository;
import com.example.course.coursemanagement.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tất cả cron job liên quan đến email.
 *
 * Cần bật @EnableScheduling trong main class hoặc config.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailScheduler {

    private final EmailQueueRepository emailQueueRepo;
    private final EmailService         emailService;
    private final EnrollmentRepository enrollmentRepo;
    private final UserRepository       userRepo;
    private final CertificateRepository certRepo;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.name:EduFlow Admin}")
    private String adminName;

    // ══════════════════════════════════════════════════════════
    //  1. PROCESS QUEUE — mỗi 30 giây
    //     Poll DB lấy batch PENDING → gửi từng cái
    // ══════════════════════════════════════════════════════════
    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void processEmailQueue() {
        List<EmailQueue> batch = emailQueueRepo.findPendingBatch(LocalDateTime.now());
        if (batch.isEmpty()) return;

        log.info("📧 Processing {} emails from queue...", batch.size());

        // Mark SENDING để tránh double-process nếu nhiều instance
        List<Long> ids = batch.stream().map(EmailQueue::getId).toList();
        emailQueueRepo.markAsSending(ids);

        // Re-fetch sau khi mark (đảm bảo status đã cập nhật)
        for (EmailQueue eq : batch) {
            // Nhỏ delay giữa các email để tránh spam trigger của Gmail
            emailService.sendOne(eq);
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
    }

    // ══════════════════════════════════════════════════════════
    //  2. REMINDER — 19:00 mỗi ngày
    //     Nhắc tất cả user có enrollment chưa hoàn thành
    // ══════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 19 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional(readOnly = true)
    public void sendDailyReminders() {
        log.info("⏰ Running daily reminder job at 19:00...");

        // Lấy tất cả user có ít nhất 1 enrollment
        List<Enrollment> allEnrollments = enrollmentRepo.findAll();

        // Group theo user, lấy khóa học đầu tiên chưa hoàn thành
        allEnrollments.stream()
                .filter(e -> e.getUser() != null
                        && e.getUser().getEmail() != null
                        && e.getCourse() != null)
                // Mỗi user chỉ nhắc 1 lần (khóa học đầu tiên)
                .collect(java.util.stream.Collectors.toMap(
                        e -> e.getUser().getId(),
                        e -> e,
                        (e1, e2) -> e1  // keep first
                ))
                .values()
                .forEach(e -> {
                    String username = e.getUser().getUsername();
                    String email    = e.getUser().getEmail();
                    String course   = e.getCourse().getTitle();

                    // Tính progress từ grade nếu có, fallback 0
                    int progress = 0;
                    if (e.getGrade() != null && e.getGrade().getScore() != null) {
                        progress = (int)(e.getGrade().getScore() * 10);
                    }

                    // Không nhắc người đã hoàn thành 100%
                    if (progress >= 100) return;

                    emailService.enqueueReminder(username, email, course, progress);
                });

        log.info("✅ Reminder emails enqueued.");
    }

    // ══════════════════════════════════════════════════════════
    //  3. WEEKLY REPORT — 08:00 thứ Hai hàng tuần
    //     Báo cáo tổng hợp tuần cho admin
    // ══════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 8 * * MON", zone = "Asia/Ho_Chi_Minh")
    @Transactional(readOnly = true)
    public void sendWeeklyAdminReport() {
        log.info("📋 Sending weekly admin report...");

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        // Tính stats tuần
        long totalUsers       = userRepo.count();
        long totalCerts       = certRepo.count();

        // Học viên đăng ký mới trong tuần (dùng ID tăng dần làm proxy)
        // Nếu có createdAt field thì dùng, không thì dùng count đơn giản
        long newUsers       = userRepo.findAll().stream()
                .filter(u -> u.getId() != null)
                .count(); // simplified — replace với query theo createdAt nếu có

        long newEnrollments = enrollmentRepo.count(); // replace với query theo enrolledAt nếu có
        long newCerts       = certRepo.count();       // replace với query theo issuedAt nếu có

        String weekLabel = LocalDateTime.now()
                .minusDays(7)
                .format(DateTimeFormatter.ofPattern("dd/MM"))
                + " - "
                + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        emailService.enqueue(
                adminEmail, adminName,
                "📋 Báo cáo tuần EduFlow · " + weekLabel,
                EmailTemplateBuilder.adminWeeklyReport(
                        newUsers, newEnrollments, newCerts,
                        totalUsers, totalCerts, weekLabel),
                EmailQueue.EmailType.ADMIN_WEEKLY
        );

        log.info("✅ Weekly report enqueued for admin.");
    }

    // ══════════════════════════════════════════════════════════
    //  4. CLEANUP — 02:00 mỗi ngày
    //     Xóa email SENT quá 30 ngày để DB không phình to
    // ══════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void cleanupOldEmails() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        emailQueueRepo.deleteOldSent(thirtyDaysAgo);
        log.info("🧹 Cleaned up emails sent before {}", thirtyDaysAgo);
    }
}