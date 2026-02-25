package com.example.course.coursemanagement.dto;

import com.example.course.coursemanagement.entity.Enrollment;
import com.example.course.coursemanagement.entity.Grade;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnrollmentDTO {
    private Long id;
    private String status;
    private LocalDateTime enrolledAt;

    // User info - flatten để tránh JsonIgnore
    private UserInfo user;
    // Course info
    private CourseInfo course;
    // Grade info
    private GradeInfo grade;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String role;
    }

    @Data
    public static class CourseInfo {
        private Long id;
        private String title;
        private String description;
        private String instructor;
        private Integer credits;
    }

    @Data
    public static class GradeInfo {
        private Long id;
        private Double score;
        private String feedback;
    }

    // Convert từ Entity sang DTO
    public static EnrollmentDTO from(Enrollment e) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(e.getId());
        dto.setStatus(e.getStatus() != null ? e.getStatus().name() : null);
        dto.setEnrolledAt(e.getEnrolledAt());

        // Map user
        if (e.getUser() != null) {
            UserInfo u = new UserInfo();
            u.setId(e.getUser().getId());
            u.setUsername(e.getUser().getUsername());
            u.setEmail(e.getUser().getEmail());
            u.setRole(e.getUser().getRole() != null ? e.getUser().getRole().name() : null);
            dto.setUser(u);
        }

        // Map course
        if (e.getCourse() != null) {
            CourseInfo c = new CourseInfo();
            c.setId(e.getCourse().getId());
            c.setTitle(e.getCourse().getTitle());
            c.setDescription(e.getCourse().getDescription());
            c.setInstructor(e.getCourse().getInstructor());
            c.setCredits(e.getCourse().getCredits());
            dto.setCourse(c);
        }

        // Map grade
        if (e.getGrade() != null) {
            GradeInfo g = new GradeInfo();
            g.setId(e.getGrade().getId());
            g.setScore(e.getGrade().getScore());
            g.setFeedback(e.getGrade().getFeedback());
            dto.setGrade(g);
        }

        return dto;
    }
}