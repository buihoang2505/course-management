package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.entity.EmailQueue;
import com.example.course.coursemanagement.entity.EmailQueue.EmailType;
import com.example.course.coursemanagement.repository.EmailQueueRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * EmailService — enqueue emails vào DB, gửi qua Gmail SMTP.
 * Không gửi trực tiếp — luôn đi qua queue để đảm bảo retry.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final EmailQueueRepository emailQueueRepo;
    private final JavaMailSender       mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.name:EduFlow Admin}")
    private String adminName;

    // ══════════════════════════════════════════════════════════
    //  ENQUEUE — thêm vào hàng đợi
    // ══════════════════════════════════════════════════════════

    @Transactional
    public void enqueue(String toEmail, String toName, String subject,
                        String htmlBody, EmailType type) {
        enqueue(toEmail, toName, subject, htmlBody, type, null);
    }

    @Transactional
    public void enqueue(String toEmail, String toName, String subject,
                        String htmlBody, EmailType type, LocalDateTime scheduledAt) {
        EmailQueue q = EmailQueue.builder()
                .toEmail(toEmail)
                .toName(toName)
                .subject(subject)
                .htmlBody(htmlBody)
                .emailType(type)
                .scheduledAt(scheduledAt)
                .build();
        emailQueueRepo.save(q);
        log.debug("Enqueued email [{}] to {}", type, toEmail);
    }

    // ══════════════════════════════════════════════════════════
    //  TRIGGERS — các event tạo email
    // ══════════════════════════════════════════════════════════

    /** Học viên đăng ký tài khoản */
    @Transactional
    public void sendWelcome(String username, String email) {
        enqueue(email, username,
                "🎉 Chào mừng " + username + " đến với EduFlow!",
                EmailTemplateBuilder.welcome(username, email),
                EmailType.WELCOME);

        // Notify admin
        enqueue(adminEmail, adminName,
                "👤 Học viên mới: " + username,
                EmailTemplateBuilder.adminNewUser(username, email,
                        emailQueueRepo.countByStatus(EmailQueue.Status.SENT)),
                EmailType.ADMIN_NEW_USER);
    }

    /** Học viên đăng ký khóa học */
    @Transactional
    public void sendEnrollment(String username, String email,
                               String courseTitle, String instructor) {
        enqueue(email, username,
                "📚 Đăng ký thành công: " + courseTitle,
                EmailTemplateBuilder.enrollment(username, courseTitle, instructor),
                EmailType.ENROLLMENT);
    }

    /** Chứng chỉ được cấp */
    @Transactional
    public void sendCertificate(String username, String email,
                                String courseTitle, String certCode,
                                int totalLessons, double avgScore) {
        String issuedDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // Gửi cho học viên
        enqueue(email, username,
                "🏆 Chứng chỉ hoàn thành: " + courseTitle,
                EmailTemplateBuilder.certificate(
                        username, courseTitle, certCode,
                        totalLessons, avgScore, issuedDate),
                EmailType.CERTIFICATE);

        // Notify admin
        long totalCerts = emailQueueRepo.countByStatus(EmailQueue.Status.SENT);
        enqueue(adminEmail, adminName,
                "🏆 Chứng chỉ mới cấp cho: " + username,
                EmailTemplateBuilder.adminNewCert(username, courseTitle, certCode, totalCerts),
                EmailType.ADMIN_NEW_CERT);
    }

    /** Học viên hoàn thành khóa học (100% progress) */
    @Transactional
    public void sendCourseCompleted(String username, String email, String courseTitle) {
        // Notify admin
        enqueue(adminEmail, adminName,
                "✅ Hoàn thành khóa học: " + username + " - " + courseTitle,
                EmailTemplateBuilder.adminCourseCompleted(username, courseTitle),
                EmailType.ADMIN_COMPLETION);
    }

    /** Nhắc nhở học tập (gọi bởi Scheduler lúc 19:00) */
    @Transactional
    public void enqueueReminder(String username, String email,
                                String courseTitle, int progressPct) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        // Tránh gửi 2 lần cùng ngày
        if (emailQueueRepo.hasReminderTodayFor(email, startOfDay)) {
            log.debug("Reminder already queued today for {}", email);
            return;
        }
        enqueue(email, username,
                "📖 Đừng quên học tập hôm nay, " + username + "!",
                EmailTemplateBuilder.reminder(username, courseTitle, progressPct),
                EmailType.REMINDER);
    }

    // ══════════════════════════════════════════════════════════
    //  SEND — gửi thực sự (gọi bởi Scheduler)
    // ══════════════════════════════════════════════════════════

    /**
     * Gửi 1 email từ queue.
     * Nếu thất bại → tăng retryCount, nếu hết retry → FAILED.
     */
    @Transactional
    public void sendOne(EmailQueue eq) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

            helper.setFrom(fromEmail, "EduFlow Academy");
            helper.setTo(eq.getToEmail());
            helper.setSubject(eq.getSubject());
            helper.setText(eq.getHtmlBody(), true); // true = HTML

            mailSender.send(msg);

            // Thành công
            eq.setStatus(EmailQueue.Status.SENT);
            eq.setSentAt(LocalDateTime.now());
            eq.setLastError(null);
            emailQueueRepo.save(eq);

            log.info("✅ Email sent [{}] → {}", eq.getEmailType(), eq.getToEmail());

        } catch (Exception e) {
            int newRetry = eq.getRetryCount() + 1;
            eq.setRetryCount(newRetry);
            eq.setLastError(e.getMessage());

            if (eq.canRetry()) {
                // Retry sau: 2^retryCount phút (1, 2, 4, 8 phút)
                long backoffMinutes = (long) Math.pow(2, newRetry - 1);
                eq.setStatus(EmailQueue.Status.PENDING);
                eq.setScheduledAt(LocalDateTime.now().plusMinutes(backoffMinutes));
                log.warn("⚠️ Email failed [retry {}/{}] to {} — retry in {}m: {}",
                        newRetry, eq.getMaxRetries(), eq.getToEmail(),
                        backoffMinutes, e.getMessage());
            } else {
                eq.setStatus(EmailQueue.Status.FAILED);
                eq.setFailedAt(LocalDateTime.now());
                log.error("❌ Email permanently failed to {}: {}", eq.getToEmail(), e.getMessage());
            }
            emailQueueRepo.save(eq);
        }
    }
}