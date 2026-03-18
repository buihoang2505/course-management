package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.LessonQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LessonQuestionRepository extends JpaRepository<LessonQuestion, Long> {

    // Lấy danh sách IDs — đơn giản, không JOIN FETCH, không ORDER BY conflict
    @Query("SELECT q.id FROM LessonQuestion q WHERE q.lesson.id = :lessonId ORDER BY q.createdAt DESC")
    List<Long> findIdsByLessonId(@Param("lessonId") Long lessonId);

    @Query("SELECT q.id FROM LessonQuestion q " +
            "JOIN q.lesson l JOIN l.course c " +
            "WHERE c.instructor = :username AND q.resolved = :resolved " +
            "ORDER BY q.createdAt DESC")
    List<Long> findIdsByInstructorUsername(@Param("username") String username,
                                           @Param("resolved") boolean resolved);

    @Query("SELECT q.id FROM LessonQuestion q WHERE q.resolved = :resolved ORDER BY q.createdAt DESC")
    List<Long> findIdsByResolved(@Param("resolved") boolean resolved);

    @Query("SELECT q.id FROM LessonQuestion q ORDER BY q.createdAt DESC")
    List<Long> findAllIds();

    // Fetch đầy đủ theo danh sách IDs (không cần ORDER BY vì sẽ sort sau)
    @Query("SELECT DISTINCT q FROM LessonQuestion q " +
            "LEFT JOIN FETCH q.user u LEFT JOIN FETCH u.profile " +
            "LEFT JOIN FETCH q.lesson l LEFT JOIN FETCH l.course " +
            "WHERE q.id IN :ids")
    List<LessonQuestion> findByIdsWithDetails(@Param("ids") List<Long> ids);

    // Fetch replies riêng theo IDs
    @Query("SELECT DISTINCT q FROM LessonQuestion q " +
            "LEFT JOIN FETCH q.replies r LEFT JOIN FETCH r.user ru LEFT JOIN FETCH ru.profile " +
            "WHERE q.id IN :ids")
    List<LessonQuestion> fetchRepliesByIds(@Param("ids") List<Long> ids);
}