package com.example.course.coursemanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * DTO khi Admin thêm hoặc cập nhật câu hỏi trong quiz.
 */
@Data
public class AddQuestionRequest {

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String content;

    private String explanation;

    @Min(1)
    private Integer points = 1;

    @NotEmpty(message = "Câu hỏi phải có ít nhất 2 lựa chọn")
    @Size(min = 2, max = 6)
    private List<ChoiceItem> choices;

    @Data
    public static class ChoiceItem {
        @NotBlank(message = "Nội dung lựa chọn không được để trống")
        private String content;

        // FIX: @JsonProperty bắt buộc Jackson map JSON "isCorrect" → field này
        // Không có annotation: Lombok @Data tạo setter setCorrect() (bỏ prefix "is")
        // → Jackson không tìm được setter → field luôn null/false → "0 đáp án đúng"
        @JsonProperty("isCorrect")
        private boolean isCorrect = false;
    }
}