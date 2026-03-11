package com.example.course.coursemanagement.dto;

import lombok.Data;
import java.util.List;

// Thống kê quiz cho admin
@Data
public class QuizStatsDTO {
    private Long   quizId;
    private String quizTitle;

    private int    totalAttempts;      // tổng số lần làm
    private int    uniqueStudents;     // số học sinh đã làm
    private double averageScore;       // điểm trung bình (0–100)
    private double passRate;           // tỉ lệ đạt (%)
    private int    highestScore;
    private int    lowestScore;

    // Phân bố điểm theo thang: 0-10, 10-20, ..., 90-100
    private List<Integer> scoreDistribution; // 10 phần tử

    // Tỉ lệ đúng từng câu
    private List<QuestionStat> questionStats;

    @Data
    public static class QuestionStat {
        private Long   questionId;
        private String questionContent;
        private long   totalAnswered;
        private long   correctCount;
        // FIX: đổi sang 0–100 để frontend so sánh nhất quán (>= 70, >= 40)
        private double correctRate;   // 0.0 – 100.0
    }
}