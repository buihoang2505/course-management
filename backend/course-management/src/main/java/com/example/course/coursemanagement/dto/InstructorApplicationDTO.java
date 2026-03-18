package com.example.course.coursemanagement.dto;

import com.example.course.coursemanagement.entity.InstructorApplication;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InstructorApplicationDTO {

    private Long   id;
    private Long   userId;
    private String username;
    private String email;
    private String fullName;
    private String avatar;
    private String experience;
    private String expertise;
    private String portfolioUrl;
    private String status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public static InstructorApplicationDTO from(InstructorApplication a) {
        var p = a.getUser().getProfile();
        return InstructorApplicationDTO.builder()
                .id(a.getId())
                .userId(a.getUser().getId())
                .username(a.getUser().getUsername())
                .email(a.getUser().getEmail())
                .fullName(p != null ? p.getFullName() : null)
                .avatar(p != null ? p.getAvatar() : null)
                .experience(a.getExperience())
                .expertise(a.getExpertise())
                .portfolioUrl(a.getPortfolioUrl())
                .status(a.getStatus().name())
                .adminNote(a.getAdminNote())
                .createdAt(a.getCreatedAt())
                .reviewedAt(a.getReviewedAt())
                .build();
    }
}