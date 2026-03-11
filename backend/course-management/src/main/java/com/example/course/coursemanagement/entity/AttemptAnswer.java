package com.example.course.coursemanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attempt_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Học sinh có chọn câu này đúng không
    @Column(name = "is_correct", nullable = false)
    @Builder.Default
    private Boolean isCorrect = false;

    // ── RELATIONSHIPS ──────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // Choice học sinh đã chọn (nullable nếu bỏ qua câu)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "choice_id")
    private Choice choice;
}