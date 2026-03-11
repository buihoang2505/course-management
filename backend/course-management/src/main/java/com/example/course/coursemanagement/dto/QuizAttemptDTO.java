package com.example.course.coursemanagement.dto;

import com.example.course.coursemanagement.entity.QuizAttempt;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuizAttemptDTO {
    private Long          id;
    private Double        score;
    private Integer       correctCount;
    private Integer       totalQuestions;
    private Boolean       passed;
    private Integer       timeSpentSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public static QuizAttemptDTO from(QuizAttempt a) {
        QuizAttemptDTO dto = new QuizAttemptDTO();
        dto.setId(a.getId());
        dto.setScore(a.getScore());
        dto.setCorrectCount(a.getCorrectCount());
        dto.setTotalQuestions(a.getTotalQuestions());
        dto.setPassed(a.getPassed());
        dto.setTimeSpentSeconds(a.getTimeSpentSeconds());
        dto.setStartedAt(a.getStartedAt());
        dto.setCompletedAt(a.getCompletedAt());
        return dto;
    }
}