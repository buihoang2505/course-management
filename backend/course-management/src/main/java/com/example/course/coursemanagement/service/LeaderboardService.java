package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.dto.LeaderboardDTO;
import com.example.course.coursemanagement.repository.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardService {

    private final LeaderboardRepository leaderboardRepo;

    public List<LeaderboardDTO> getLeaderboard() {
        List<Object[]> rows = leaderboardRepo.findLeaderboardRaw();
        List<LeaderboardDTO> result = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Object[] r = rows.get(i);

            long   completedCourses = r[5] != null ? ((Number) r[5]).longValue() : 0;
            long   totalCourses     = r[6] != null ? ((Number) r[6]).longValue() : 0;
            long   completedLessons = r[7] != null ? ((Number) r[7]).longValue() : 0;
            double avgScore         = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;

            // Công thức điểm tổng hợp:
            // avgScore (0-10)  × 5  → max 50
            // completedCourses × 10 → không giới hạn
            // completedLessons × 1  → không giới hạn
            double totalPoints = (avgScore * 5) + (completedCourses * 10) + (completedLessons);

            result.add(LeaderboardDTO.builder()
                    .rank(i + 1)
                    .userId(((Number) r[0]).longValue())
                    .username((String) r[1])
                    .fullName((String) r[2])
                    .avatar((String) r[3])
                    .avgScore(Math.round(avgScore * 10.0) / 10.0)
                    .completedCourses(completedCourses)
                    .totalCourses(totalCourses)
                    .completedLessons(completedLessons)
                    .totalPoints(Math.round(totalPoints * 10.0) / 10.0)
                    .build());
        }

        // Sort lại theo totalPoints
        result.sort((a, b) -> Double.compare(b.getTotalPoints(), a.getTotalPoints()));
        for (int i = 0; i < result.size(); i++) result.get(i).setRank(i + 1);

        return result;
    }
}