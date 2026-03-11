package com.example.course.coursemanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Email queue lưu vào DB — an toàn khi server restart.
 * Scheduler sẽ poll bảng này và gửi từng batch.
 */
@Entity
@Table(name = "email_queue", indexes = {
        @Index(name = "idx_eq_status",    columnList = "status"),
        @Index(name = "idx_eq_scheduled", columnList = "scheduled_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Người nhận ─────────────────────────────────────────────
    @Column(nullable = false, length = 150)
    private String toEmail;

    @Column(length = 100)
    private String toName;

    // ── Nội dung ───────────────────────────────────────────────
    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String htmlBody;

    // ── Loại email (để log / filter) ───────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    @Builder.Default
    private EmailType emailType = EmailType.GENERAL;

    // ── Trạng thái ─────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    // ── Retry ──────────────────────────────────────────────────
    @Builder.Default
    private int retryCount = 0;

    @Builder.Default
    private int maxRetries = 4;   // tổng 5 lần (lần đầu + 4 retry)

    @Column(columnDefinition = "TEXT")
    private String lastError;

    // ── Thời gian ──────────────────────────────────────────────
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** null = gửi ngay, có giá trị = chờ đến lúc này mới gửi */
    private LocalDateTime scheduledAt;

    private LocalDateTime sentAt;
    private LocalDateTime failedAt;

    // ── Enums ──────────────────────────────────────────────────
    public enum Status {
        PENDING,    // chờ gửi
        SENDING,    // đang xử lý (tránh double-send)
        SENT,       // thành công
        FAILED      // hết retry
    }

    public enum EmailType {
        WELCOME,            // chào mừng đăng ký
        ENROLLMENT,         // đăng ký khóa học thành công
        CERTIFICATE,        // chứng chỉ được cấp
        COURSE_COMPLETED,   // hoàn thành khóa học
        REMINDER,           // nhắc nhở học tập 19h
        ADMIN_NEW_USER,     // admin: học viên mới
        ADMIN_NEW_CERT,     // admin: chứng chỉ mới cấp
        ADMIN_COMPLETION,   // admin: học viên hoàn thành
        ADMIN_WEEKLY,       // admin: báo cáo tuần
        GENERAL
    }

    /** Có thể retry tiếp không? */
    public boolean canRetry() {
        return retryCount < maxRetries;
    }
}