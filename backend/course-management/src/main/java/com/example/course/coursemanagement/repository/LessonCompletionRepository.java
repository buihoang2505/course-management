package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.LessonCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, Long> {

    // Kiểm tra đã hoàn thành bài học chưa
    boolean existsByUserIdAndLessonId(Long userId, Long lessonId);

    // Lấy tất cả completion của user trong 1 course
    @Query("""
        SELECT lc FROM LessonCompletion lc
        WHERE lc.user.id = :userId
          AND lc.lesson.course.id = :courseId
        """)
    List<LessonCompletion> findByUserIdAndCourseId(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId
    );

    // Đếm số bài đã hoàn thành của user trong course
    @Query("""
        SELECT COUNT(lc) FROM LessonCompletion lc
        WHERE lc.user.id = :userId
          AND lc.lesson.course.id = :courseId
        """)
    long countByUserIdAndCourseId(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId
    );

    // Lấy danh sách lessonId đã hoàn thành của user trong course
    @Query("""
        SELECT lc.lesson.id FROM LessonCompletion lc
        WHERE lc.user.id = :userId
          AND lc.lesson.course.id = :courseId
        """)
    List<Long> findCompletedLessonIdsByUserAndCourse(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId
    );

    // Xóa 1 completion (unmark)
    Optional<LessonCompletion> findByUserIdAndLessonId(Long userId, Long lessonId);

    // Dùng khi xóa user
    void deleteByUserId(Long userId);

    /** Xóa completions của user trong 1 course khi unenroll */
    @Modifying
    @Query("DELETE FROM LessonCompletion lc WHERE lc.user.id = :userId AND lc.lesson.course.id = :courseId")
    void deleteByUserIdAndCourseId(@Param("userId") Long userId,
                                   @Param("courseId") Long courseId);
}