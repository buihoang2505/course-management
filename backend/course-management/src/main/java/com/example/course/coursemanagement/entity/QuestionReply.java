package com.example.course.coursemanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name = "question_replies",
        indexes = @Index(name="idx_qr_question", columnList="question_id"))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class QuestionReply {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private LessonQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** true nếu người trả lời là INSTRUCTOR hoặc ADMIN */
    @Builder.Default
    private Boolean official = false;

    @CreationTimestamp @Column(updatable = false)
    private LocalDateTime createdAt;
}