package com.example.course.coursemanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "instructor_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Mô tả kinh nghiệm / lý do muốn trở thành giảng viên */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String experience;

    /** Lĩnh vực chuyên môn: "Java, Spring Boot, Docker" */
    @Column(length = 300)
    private String expertise;

    /** GitHub / LinkedIn / Portfolio — optional */
    @Column(length = 500)
    private String portfolioUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    /** Admin ghi lý do khi reject */
    @Column(columnDefinition = "TEXT")
    private String adminNote;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    public enum Status { PENDING, APPROVED, REJECTED }
}