package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    // Lấy enrollments của user, kèm course
    @Query("SELECT e FROM Enrollment e LEFT JOIN FETCH e.course WHERE e.user.id = :userId")
    List<Enrollment> findByUserIdWithCourse(@Param("userId") Long userId);

    // Lấy enrollments của course, kèm user VÀ grade (quan trọng!)
    @Query("SELECT e FROM Enrollment e " +
            "LEFT JOIN FETCH e.user " +
            "LEFT JOIN FETCH e.grade " +
            "WHERE e.course.id = :courseId")
    List<Enrollment> findByCourseIdWithUserAndGrade(@Param("courseId") Long courseId);
}