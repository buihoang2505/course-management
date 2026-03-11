package com.example.course.coursemanagement.dto;

import com.example.course.coursemanagement.entity.Role;
import com.example.course.coursemanagement.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {

    private Long          id;
    private String        username;
    private String        email;
    private String        role;        // String để frontend dùng dễ
    private LocalDateTime createdAt;

    // Không có password, không có enrollments, không có profile
    // → tránh hoàn toàn circular reference khi serialize JSON

    public static UserDTO from(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole() != null ? u.getRole().name() : "STUDENT");
        dto.setCreatedAt(u.getCreatedAt());
        return dto;
    }
}