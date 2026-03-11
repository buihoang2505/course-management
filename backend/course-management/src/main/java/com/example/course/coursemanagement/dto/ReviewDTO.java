package com.example.course.coursemanagement.dto;

import com.example.course.coursemanagement.entity.Review;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String username;
    private String avatar;
    private Long courseId;
    private String courseTitle;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewDTO from(Review r) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(r.getId());
        dto.setUserId(r.getUser().getId());
        dto.setUsername(r.getUser().getUsername());
        // avatar từ profile nếu có
        var profile = r.getUser().getProfile();
        dto.setAvatar(profile != null ? profile.getAvatar() : null);
        dto.setCourseId(r.getCourse().getId());
        dto.setCourseTitle(r.getCourse().getTitle());
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}