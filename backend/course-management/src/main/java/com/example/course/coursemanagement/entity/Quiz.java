package com.example.course.coursemanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_quiz_lesson", columnNames = "lesson_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // KHÔNG dùng @NotBlank ở entity vì khi tạo qua DTO sẽ validate ở đó
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Min(value = 1)
    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Min(0) @Max(100)
    // FIX: thêm columnDefinition để DB có DEFAULT, tránh NULL khi insert
    @Column(name = "passing_score", nullable = false, columnDefinition = "INT DEFAULT 60")
    @Builder.Default
    private Integer passingScore = 60;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    // FIX: columnDefinition rõ ràng → DB luôn có DEFAULT 1
    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── RELATIONSHIPS ──────────────────────────────────

    // 1 lesson chỉ có đúng 1 quiz — enforce bằng @OneToOne + UNIQUE constraint ở DB
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false, unique = true)
    private Lesson lesson;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderNum ASC")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QuizAttempt> attempts = new ArrayList<>();
}