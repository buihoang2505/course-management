package com.example.course.coursemanagement.dto;

import com.example.course.coursemanagement.entity.Question;
import com.example.course.coursemanagement.entity.Quiz;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO đầy đủ cho admin edit quiz — bao gồm questions + choices + isCorrect.
 * Dùng cho endpoint GET /api/quizzes/{id}/admin/detail
 */
@Data
public class QuizDetailAdminDTO {
    private Long    id;
    private String  title;
    private String  description;
    private Integer timeLimitMinutes;
    private Integer passingScore;
    private Integer maxAttempts;
    private Boolean isActive;
    private Long    lessonId;
    private String  lessonTitle;
    private Long    courseId;
    private String  courseTitle;
    private List<QuestionAdminDTO> questions;

    public static QuizDetailAdminDTO from(Quiz q, List<Question> questions) {
        QuizDetailAdminDTO dto = new QuizDetailAdminDTO();
        dto.setId(q.getId());
        dto.setTitle(q.getTitle());
        dto.setDescription(q.getDescription());
        dto.setTimeLimitMinutes(q.getTimeLimitMinutes());
        dto.setPassingScore(q.getPassingScore());
        dto.setMaxAttempts(q.getMaxAttempts());
        dto.setIsActive(q.getIsActive());
        if (q.getLesson() != null) {
            dto.setLessonId(q.getLesson().getId());
            dto.setLessonTitle(q.getLesson().getTitle());
            if (q.getLesson().getCourse() != null) {
                dto.setCourseId(q.getLesson().getCourse().getId());
                dto.setCourseTitle(q.getLesson().getCourse().getTitle());
            }
        }
        dto.setQuestions(questions.stream()
                .map(QuestionAdminDTO::from)
                .collect(Collectors.toList()));
        return dto;
    }
}