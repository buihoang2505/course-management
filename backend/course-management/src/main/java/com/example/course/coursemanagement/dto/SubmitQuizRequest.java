package com.example.course.coursemanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

// Request body khi học sinh submit quiz
@Data
public class SubmitQuizRequest {

    @NotNull
    private Long userId;

    // Thời gian làm bài thực tế (giây), frontend tính
    private Integer timeSpentSeconds;

    // Danh sách câu trả lời
    @NotNull
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        @NotNull
        private Long questionId;

        // null nếu học sinh bỏ qua câu này
        private Long choiceId;
    }
}