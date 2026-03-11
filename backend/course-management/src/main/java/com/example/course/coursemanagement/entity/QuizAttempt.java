package com.example.course.coursemanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Điểm số (0–100 scale sau khi tính %)
    @Column(nullable = false)
    @Builder.Default
    private Double score = 0.0;

    // Số câu đúng
    @Column(name = "correct_count", nullable = false)
    @Builder.Default
    private Integer correctCount = 0;

    // Tổng số câu
    @Column(name = "total_questions", nullable = false)
    @Builder.Default
    private Integer totalQuestions = 0;

    // Đạt hay không (score >= passingScore)
    @Column(nullable = false)
    @Builder.Default
    private Boolean passed = false;

    // Thời gian làm bài (giây)
    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @CreationTimestamp
    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ── RELATIONSHIPS ──────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Câu trả lời của lần làm này
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AttemptAnswer> answers = new ArrayList<>();
}