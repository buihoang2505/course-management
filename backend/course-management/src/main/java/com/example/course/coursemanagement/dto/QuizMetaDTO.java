package com.example.course.coursemanagement.dto;

import com.example.course.coursemanagement.entity.Quiz;
import lombok.Data;

/**
 * Metadata quiz trả cho học viên khi hiển thị danh sách bài học.
 * KHÔNG chứa questions/choices để tránh lộ đáp án và tránh response nặng.
 * Dùng cho: CourseDetailView accordion — hiện block "🧩 Quiz" mỗi bài học.
 */
@Data
public class QuizMetaDTO {
    private Long    id;
    private String  title;
    private String  description;
    private Integer timeLimitMinutes;
    private Integer passingScore;
    private Integer maxAttempts;
    private int     questionCount;
    private Boolean isActive;

    public static QuizMetaDTO from(Quiz q) {
        QuizMetaDTO dto = new QuizMetaDTO();
        dto.setId(q.getId());
        dto.setTitle(q.getTitle());
        dto.setDescription(q.getDescription());
        dto.setTimeLimitMinutes(q.getTimeLimitMinutes());
        dto.setPassingScore(q.getPassingScore());
        dto.setMaxAttempts(q.getMaxAttempts());
        dto.setIsActive(q.getIsActive());
        dto.setQuestionCount(q.getQuestions() != null ? q.getQuestions().size() : 0);
        return dto;
    }
}