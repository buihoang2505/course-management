package com.example.course.coursemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.course.coursemanagement.entity.User;

import java.util.List;

public interface LeaderboardRepository extends JpaRepository<User, Long> {

    // Lấy toàn bộ dữ liệu leaderboard:
    // userId, username, fullName, avatar, avgScore, completedCourses, totalCourses, completedLessons
    @Query("""
        SELECT
            u.id,
            u.username,
            COALESCE(p.fullName, ''),
            COALESCE(p.avatar, ''),
            COALESCE(AVG(g.score), 0.0),
            SUM(CASE WHEN e.status = 'COMPLETED' THEN 1 ELSE 0 END),
            COUNT(DISTINCT e.id),
            COALESCE((SELECT COUNT(lc) FROM LessonCompletion lc WHERE lc.user.id = u.id), 0)
        FROM User u
        LEFT JOIN u.profile p
        LEFT JOIN u.enrollments e
        LEFT JOIN e.grade g
        WHERE u.role = 'STUDENT'
        GROUP BY u.id, u.username, p.fullName, p.avatar
        HAVING COUNT(DISTINCT e.id) > 0
        ORDER BY COALESCE(AVG(g.score), 0) DESC, SUM(CASE WHEN e.status = 'COMPLETED' THEN 1 ELSE 0 END) DESC
        """)
    List<Object[]> findLeaderboardRaw();
}