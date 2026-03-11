package com.example.course.coursemanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Snapshot chứng chỉ tại thời điểm đạt — độc lập với quiz/lesson sau này.
 * Admin sửa quiz sau không ảnh hưởng chứng chỉ đã cấp.
 */
@Entity
@Table(
        name = "certificates",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cert_user_course",
                columnNames = {"user_id", "course_id"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã unique để verify — UUID uppercase
    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String certificateCode;

    // ── Snapshots tại thời điểm cấp (không bao giờ thay đổi) ──
    @Column(nullable = false)
    private String courseTitle;

    @Column(nullable = false)
    private String studentName;

    @Column(nullable = false)
    @Builder.Default
    private Double averageQuizScore = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalLessons = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime issuedAt;

    // ── Relationships ──────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @PrePersist
    void prePersist() {
        if (certificateCode == null) {
            certificateCode = UUID.randomUUID().toString().toUpperCase();
        }
    }
}