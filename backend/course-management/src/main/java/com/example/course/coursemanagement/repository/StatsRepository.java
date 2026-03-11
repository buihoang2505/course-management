package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StatsRepository extends JpaRepository<Enrollment, Long> {

    // Đếm enrollment theo tháng trong năm hiện tại
    @Query("""
        SELECT MONTH(e.enrolledAt), COUNT(e)
        FROM Enrollment e
        WHERE YEAR(e.enrolledAt) = :year
        GROUP BY MONTH(e.enrolledAt)
        ORDER BY MONTH(e.enrolledAt)
        """)
    List<Object[]> countEnrollmentsByMonth(@Param("year") int year);

    // Top 5 khóa học nhiều học viên nhất
    @Query("""
        SELECT c.title, COUNT(e), AVG(g.score)
        FROM Enrollment e
        JOIN e.course c
        LEFT JOIN Grade g ON g.enrollment = e
        GROUP BY c.id, c.title
        ORDER BY COUNT(e) DESC
        LIMIT 5
        """)
    List<Object[]> findTopCourses();

    // Phân bổ điểm
    @Query("SELECT COUNT(g) FROM Grade g WHERE g.score >= 8.5")
    long countExcellent();

    @Query("SELECT COUNT(g) FROM Grade g WHERE g.score >= 7 AND g.score < 8.5")
    long countGood();

    @Query("SELECT COUNT(g) FROM Grade g WHERE g.score >= 5 AND g.score < 7")
    long countAverage();

    @Query("SELECT COUNT(g) FROM Grade g WHERE g.score < 5")
    long countPoor();

    // Điểm trung bình toàn hệ thống
    @Query("SELECT COALESCE(AVG(g.score), 0) FROM Grade g")
    double findOverallAvgScore();

    // Tỉ lệ hoàn thành
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.status = 'COMPLETED'")
    long countCompleted();
}