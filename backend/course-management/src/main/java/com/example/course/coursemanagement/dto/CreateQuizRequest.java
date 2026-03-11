package com.example.course.coursemanagement.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO dùng khi Admin TẠO hoặc CẬP NHẬT quiz.
 * Không nhận trực tiếp entity Quiz để tránh mass assignment
 * và tránh @Builder.Default bị bỏ qua khi deserialize JSON.
 */
@Data
public class CreateQuizRequest {

    @NotBlank(message = "Tiêu đề quiz không được để trống")
    @Size(max = 200)
    private String title;

    private String description;

    @Min(value = 1, message = "Thời gian tối thiểu 1 phút")
    private Integer timeLimitMinutes;   // null = không giới hạn

    @Min(0) @Max(100)
    private Integer passingScore = 60;  // default 60%

    @Min(1)
    private Integer maxAttempts;        // null = không giới hạn

    private Boolean isActive = true;    // default active
}