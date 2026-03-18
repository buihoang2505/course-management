package com.example.course.coursemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = @UniqueConstraint(name = "uk_enroll_user_course", columnNames = {"user_id", "course_id"}),
        indexes = {
                @Index(name = "idx_enroll_user",   columnList = "user_id"),
                @Index(name = "idx_enroll_course", columnList = "course_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime enrolledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    // ============ RELATIONSHIPS ============

    // @ManyToOne – Nhiều Enrollment thuộc về 1 User
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // @ManyToOne – Nhiều Enrollment thuộc về 1 Course
    // => Kết hợp 2 @ManyToOne này = hiệu ứng @ManyToMany User <-> Course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // @OneToOne – Mỗi Enrollment có 1 Grade (điểm số)
    @JsonIgnore
    @OneToOne(mappedBy = "enrollment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Grade grade;

    // ============ ENUM ============

    public enum EnrollmentStatus {
        ACTIVE,     // Đang học
        COMPLETED,  // Đã hoàn thành
        DROPPED     // Đã hủy đăng ký
    }
    @Enumerated(EnumType.STRING)
    private ResultStatus result;

    private Double finalScore;

    private LocalDateTime completedAt;

    public enum ResultStatus {
        PASSED,
        FAILED
    }
}