package com.example.course.coursemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardDTO {
    private Long   userId;
    private String username;
    private String fullName;
    private String avatar;
    private int    rank;

    // Điểm TB (từ Grade)
    private Double avgScore;

    // Số khóa đã hoàn thành
    private long completedCourses;

    // Tổng số khóa đã enroll
    private long totalCourses;

    // Tổng bài học đã hoàn thành (LessonCompletion)
    private long completedLessons;

    // Điểm tổng hợp để xếp hạng (weighted score)
    private double totalPoints;
}