package com.example.course.coursemanagement.dto;

import com.example.course.coursemanagement.entity.AttemptAnswer;
import com.example.course.coursemanagement.entity.QuizAttempt;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// Trả về sau khi chấm điểm — CÓ đáp án đúng + giải thích
@Data
public class QuizResultDTO {
    private Long          attemptId;
    private Double        score;         // 0–100
    private Integer       correctCount;
    private Integer       totalQuestions;
    private Boolean       passed;
    private Integer       timeSpentSeconds;
    private LocalDateTime completedAt;
    private List<AnswerResultDTO> answers;

    @Data
    public static class AnswerResultDTO {
        private Long    questionId;
        private String  questionContent;
        private String  explanation;
        private Long    selectedChoiceId;   // học sinh chọn
        private Long    correctChoiceId;    // đáp án đúng
        private Boolean isCorrect;
    }

    public static QuizResultDTO from(QuizAttempt attempt) {
        QuizResultDTO dto = new QuizResultDTO();
        dto.setAttemptId(attempt.getId());
        dto.setScore(attempt.getScore());
        dto.setCorrectCount(attempt.getCorrectCount());
        dto.setTotalQuestions(attempt.getTotalQuestions());
        dto.setPassed(attempt.getPassed());
        dto.setTimeSpentSeconds(attempt.getTimeSpentSeconds());
        dto.setCompletedAt(attempt.getCompletedAt());

        dto.setAnswers(attempt.getAnswers().stream().map(aa -> {
            AnswerResultDTO a = new AnswerResultDTO();
            a.setQuestionId(aa.getQuestion().getId());
            a.setQuestionContent(aa.getQuestion().getContent());
            a.setExplanation(aa.getQuestion().getExplanation());
            a.setSelectedChoiceId(aa.getChoice() != null ? aa.getChoice().getId() : null);
            a.setIsCorrect(aa.getIsCorrect());

            // Tìm đáp án đúng trong danh sách choices
            aa.getQuestion().getChoices().stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsCorrect()))
                    .findFirst()
                    .ifPresent(c -> a.setCorrectChoiceId(c.getId()));

            return a;
        }).collect(Collectors.toList()));

        return dto;
    }
}