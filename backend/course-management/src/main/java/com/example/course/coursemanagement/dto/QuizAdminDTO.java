package com.example.course.coursemanagement.dto;

import com.example.course.coursemanagement.entity.Quiz;
import lombok.Data;

@Data
public class QuizAdminDTO {
    private Long    id;
    private String  title;
    private String  description;
    private Integer timeLimitMinutes;
    private Integer passingScore;
    private Integer maxAttempts;
    private Boolean isActive;
    private int     questionCount;
    private Long    lessonId;
    private String  lessonTitle;
    private Integer lessonOrderNum;
    private Long    courseId;
    private String  courseTitle;

    public static QuizAdminDTO from(Quiz q) {
        QuizAdminDTO dto = new QuizAdminDTO();
        dto.setId(q.getId());
        dto.setTitle(q.getTitle());
        dto.setDescription(q.getDescription());
        dto.setTimeLimitMinutes(q.getTimeLimitMinutes());
        dto.setPassingScore(q.getPassingScore());
        dto.setMaxAttempts(q.getMaxAttempts());
        dto.setIsActive(q.getIsActive());
        dto.setQuestionCount(q.getQuestions() != null ? q.getQuestions().size() : 0);
        if (q.getLesson() != null) {
            dto.setLessonId(q.getLesson().getId());
            dto.setLessonTitle(q.getLesson().getTitle());
            dto.setLessonOrderNum(q.getLesson().getOrderNum());
            if (q.getLesson().getCourse() != null) {
                dto.setCourseId(q.getLesson().getCourse().getId());
                dto.setCourseTitle(q.getLesson().getCourse().getTitle());
            }
        }
        return dto;
    }
}