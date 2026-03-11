package com.example.course.coursemanagement.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AdminStatsDTO {

    // ── Tổng quan ──
    private long totalUsers;
    private long totalCourses;
    private long totalEnrollments;
    private long totalCompletions;    // enrollments COMPLETED
    private double avgScore;          // điểm TB toàn hệ thống

    // ── Biểu đồ: đăng ký theo tháng (12 tháng gần nhất) ──
    private List<MonthStat> enrollmentsByMonth;

    // ── Biểu đồ: top 5 khóa học nhiều học viên nhất ──
    private List<CourseStat> topCourses;

    // ── Biểu đồ: phân bổ điểm ──
    private ScoreDistribution scoreDistribution;

    // ── Biểu đồ: tỉ lệ hoàn thành ──
    private double completionRate; // %

    @Data
    @Builder
    public static class MonthStat {
        private String month;   // "T1", "T2", ...
        private long count;
    }

    @Data
    @Builder
    public static class CourseStat {
        private String title;
        private long enrollments;
        private Double avgScore;
    }

    @Data
    @Builder
    public static class ScoreDistribution {
        private long excellent;  // >= 8.5
        private long good;       // 7 - 8.4
        private long average;    // 5 - 6.9
        private long poor;       // < 5
    }
}