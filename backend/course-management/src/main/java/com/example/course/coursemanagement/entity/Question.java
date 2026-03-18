package com.example.course.coursemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Giải thích đáp án đúng (hiển thị sau khi làm xong)
    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Min(1)
    @Column(name = "order_num", nullable = false)
    @Builder.Default
    private Integer orderNum = 1;

    // Điểm của câu hỏi này (mặc định 1, có thể tuỳ chỉnh)
    @Column(nullable = false)
    @Builder.Default
    private Integer points = 1;

    // ── RELATIONSHIPS ──────────────────────────────────

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // Mỗi câu hỏi có nhiều lựa chọn (thường 4)
    @ToString.Exclude
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    @Builder.Default
    private List<Choice> choices = new ArrayList<>();
}