package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Tìm tất cả khóa học đang ACTIVE
    List<Course> findByStatus(Course.CourseStatus status);

    // Tìm khóa học theo tên (tìm kiếm)
    List<Course> findByTitleContainingIgnoreCase(String keyword);

    // Tìm course kèm lessons (tránh N+1 query)
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.lessons WHERE c.id = :id")
    Optional<Course> findByIdWithLessons(Long id);

    // Đếm số học viên của 1 khóa học
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    Long countEnrollmentsByCourseId(Long courseId);
}