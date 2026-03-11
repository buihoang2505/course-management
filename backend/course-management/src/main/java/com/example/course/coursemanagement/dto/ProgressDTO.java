package com.example.course.coursemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProgressDTO {

    private Long courseId;
    private String courseTitle;

    private int totalLessons;      // Tổng số bài học
    private long completedLessons; // Số bài đã hoàn thành
    private int percentage;        // % tiến độ (0-100)

    private boolean courseCompleted; // Đã xong toàn bộ chưa

    private List<Long> completedLessonIds; // Danh sách lesson đã xong

    // Factory method tính % tự động
    public static ProgressDTO of(
            Long courseId,
            String courseTitle,
            int totalLessons,
            long completedLessons,
            List<Long> completedLessonIds
    ) {
        int pct = totalLessons == 0 ? 0
                : (int) Math.round((completedLessons * 100.0) / totalLessons);

        return new ProgressDTO(
                courseId,
                courseTitle,
                totalLessons,
                completedLessons,
                pct,
                completedLessons >= totalLessons && totalLessons > 0,
                completedLessonIds
        );
    }
}