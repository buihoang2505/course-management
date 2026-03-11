package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.dto.AdminStatsDTO;
import com.example.course.coursemanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private final UserRepository       userRepository;
    private final CourseRepository     courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StatsRepository      statsRepository;

    public AdminStatsDTO getStats() {
        int year = LocalDate.now().getYear();

        // ── Tổng quan ──
        long totalUsers       = userRepository.count();
        long totalCourses     = courseRepository.count();
        long totalEnrollments = enrollmentRepository.count();
        long totalCompletions = statsRepository.countCompleted();
        double avgScore       = statsRepository.findOverallAvgScore();
        double completionRate = totalEnrollments == 0 ? 0
                : Math.round((totalCompletions * 100.0) / totalEnrollments * 10) / 10.0;

        // ── Đăng ký theo tháng ──
        List<Object[]> monthRaw = statsRepository.countEnrollmentsByMonth(year);
        Map<Integer, Long> monthMap = new HashMap<>();
        for (Object[] row : monthRaw) {
            monthMap.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        List<AdminStatsDTO.MonthStat> byMonth = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            byMonth.add(AdminStatsDTO.MonthStat.builder()
                    .month("T" + m)
                    .count(monthMap.getOrDefault(m, 0L))
                    .build());
        }

        // ── Top courses ──
        List<AdminStatsDTO.CourseStat> topCourses = new ArrayList<>();
        for (Object[] row : statsRepository.findTopCourses()) {
            topCourses.add(AdminStatsDTO.CourseStat.builder()
                    .title((String) row[0])
                    .enrollments(((Number) row[1]).longValue())
                    .avgScore(row[2] != null ? ((Number) row[2]).doubleValue() : null)
                    .build());
        }

        // ── Score distribution ──
        AdminStatsDTO.ScoreDistribution dist = AdminStatsDTO.ScoreDistribution.builder()
                .excellent(statsRepository.countExcellent())
                .good(statsRepository.countGood())
                .average(statsRepository.countAverage())
                .poor(statsRepository.countPoor())
                .build();

        return AdminStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalCourses(totalCourses)
                .totalEnrollments(totalEnrollments)
                .totalCompletions(totalCompletions)
                .avgScore(Math.round(avgScore * 10) / 10.0)
                .enrollmentsByMonth(byMonth)
                .topCourses(topCourses)
                .scoreDistribution(dist)
                .completionRate(completionRate)
                .build();
    }
}