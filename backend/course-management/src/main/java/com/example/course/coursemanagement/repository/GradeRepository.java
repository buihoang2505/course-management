package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    // Tìm điểm theo enrollment
    Optional<Grade> findByEnrollmentId(Long enrollmentId);

    // Lấy tất cả điểm của 1 user
    List<Grade> findByEnrollment_User_Id(Long userId);

    // Tính điểm trung bình của 1 khóa học
    @Query("SELECT AVG(g.score) FROM Grade g WHERE g.enrollment.course.id = :courseId")
    Double findAverageScoreByCourseId(@Param("courseId") Long courseId);
}