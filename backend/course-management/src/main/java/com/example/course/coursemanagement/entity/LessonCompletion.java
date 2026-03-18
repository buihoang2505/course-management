package com.example.course.coursemanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "lesson_completions",
        uniqueConstraints = @UniqueConstraint(name = "uk_lc_user_lesson", columnNames = {"user_id", "lesson_id"}),
        indexes = {
                @Index(name = "idx_lc_user",   columnList = "user_id"),
                @Index(name = "idx_lc_lesson", columnList = "lesson_id")
        }
)
@Data
public class LessonCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    @PrePersist
    void prePersist() {
        completedAt = LocalDateTime.now();
    }
}