package com.example.course.coursemanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tiêu đề bài học không được để trống")
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Min(value = 1, message = "Thứ tự bài học phải >= 1")
    @Column(nullable = false)
    private Integer orderNum = 1;

    @Column(length = 255)
    private String videoUrl; // Link video bài giảng (nếu có)

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // ============ RELATIONSHIPS ============

    // @ManyToOne – Nhiều Lesson thuộc về 1 Course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}