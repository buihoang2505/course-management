package com.example.course.coursemanagement.dto;

import com.example.course.coursemanagement.entity.Quiz;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

// Dùng khi học sinh vào làm bài — không lộ đáp án
@Data
public class QuizDTO {
    private Long             id;
    private String           title;
    private String           description;
    private Integer          timeLimitMinutes;
    private Integer          passingScore;
    private Integer          maxAttempts;
    private int              questionCount;
    private List<QuestionDTO> questions;
    private boolean           canAttempt = true; // frontend dùng để disable nút

    public static QuizDTO from(Quiz q) {
        QuizDTO dto = new QuizDTO();
        dto.setId(q.getId());
        dto.setTitle(q.getTitle());
        dto.setDescription(q.getDescription());
        dto.setTimeLimitMinutes(q.getTimeLimitMinutes());
        dto.setPassingScore(q.getPassingScore());
        dto.setMaxAttempts(q.getMaxAttempts());
        dto.setQuestionCount(q.getQuestions().size());
        dto.setQuestions(q.getQuestions().stream()
                .map(QuestionDTO::from)
                .collect(Collectors.toList()));
        return dto;
    }
}