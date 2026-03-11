package com.example.course.coursemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// ===== REQUEST: Đăng nhập bằng USERNAME =====
@Data
public class LoginRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotBlank(message = "Password không được để trống")
    private String password;
}