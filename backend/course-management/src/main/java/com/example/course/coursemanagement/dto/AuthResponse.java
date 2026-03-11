package com.example.course.coursemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ===== RESPONSE: Trả về sau login/register =====
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long   id;        // ← cần thiết để frontend gọi API theo userId
    private String token;
    private String email;
    private String username;
    private String role;
    private String message;
    private String avatar;  // URL avatar từ profile
}