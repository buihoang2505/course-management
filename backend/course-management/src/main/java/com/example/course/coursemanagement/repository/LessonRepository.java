package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // Lấy lessons của course, sort theo thứ tự
    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.course WHERE l.course.id = :courseId ORDER BY l.orderNum")
    List<Lesson> findByCourseIdOrderByOrderNum(@Param("courseId") Long courseId);

    // Đếm tổng số bài học của course (dùng cho tính điểm tự động)
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.course.id = :courseId")
    int countByCourseId(@Param("courseId") Long courseId);
}