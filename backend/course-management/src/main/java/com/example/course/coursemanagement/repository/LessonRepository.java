package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // Lấy tất cả bài học của 1 khóa học, sắp xếp theo thứ tự
    List<Lesson> findByCourseIdOrderByOrderNumAsc(Long courseId);

    // Đếm số bài học trong 1 khóa học
    int countByCourseId(Long courseId);
}