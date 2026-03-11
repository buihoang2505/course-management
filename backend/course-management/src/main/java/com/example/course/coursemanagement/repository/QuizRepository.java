package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.Question;
import com.example.course.coursemanagement.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // ════════════════════════════════════════════════════
    //  STUDENT
    // ════════════════════════════════════════════════════

    /** Tìm quiz đơn giản theo lessonId — KHÔNG JOIN collection, an toàn với Optional */
    @Query("SELECT q FROM Quiz q " +
            "WHERE q.lesson.id = :lessonId " +
            "AND (q.isActive = true OR q.isActive IS NULL)")
    Optional<Quiz> findActiveByLessonId(@Param("lessonId") Long lessonId);

    /**
     * Tìm quiz + questions theo lessonId — trả List để tránh IncorrectResultSizeDataAccessException
     * khi JOIN FETCH collection sinh nhiều rows (1 quiz + N questions = N rows)
     */
    @Query("SELECT DISTINCT q FROM Quiz q " +
            "LEFT JOIN FETCH q.questions qs " +
            "WHERE q.lesson.id = :lessonId " +
            "AND (q.isActive = true OR q.isActive IS NULL)")
    List<Quiz> findActiveByLessonIdWithQuestions(@Param("lessonId") Long lessonId);

    /**
     * Query 1/2 — Quiz + questions (KHÔNG fetch choices).
     * Tránh MultipleBagFetchException khi JOIN FETCH 2 List cùng lúc.
     * Dùng cho: submitQuiz, getStats, getQuizForStudent (step 1)
     */
    @Query("SELECT DISTINCT q FROM Quiz q " +
            "LEFT JOIN FETCH q.questions qs " +
            "WHERE q.id = :id " +
            "AND (q.isActive = true OR q.isActive IS NULL)")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);

    /**
     * Query 2/2 — Questions + choices theo quizId, không lọc isActive.
     * Kết hợp với findByIdWithQuestions để có đủ data.
     * Trả List<Question> (không phải Quiz) để tránh duplicate.
     */
    @Query("SELECT DISTINCT q FROM Question q " +
            "LEFT JOIN FETCH q.choices " +
            "WHERE q.quiz.id = :quizId " +
            "ORDER BY q.orderNum ASC")
    List<Question> findQuestionsWithChoicesByQuizId(@Param("quizId") Long quizId);

    // ════════════════════════════════════════════════════
    //  ADMIN
    // ════════════════════════════════════════════════════

    /** Tất cả quiz kèm lesson + course (trang quản lý). */
    @Query("SELECT DISTINCT q FROM Quiz q " +
            "LEFT JOIN FETCH q.lesson l " +
            "LEFT JOIN FETCH l.course")
    List<Quiz> findAllWithLessonAndCourse();

    /** Quiz theo courseId. */
    @Query("SELECT DISTINCT q FROM Quiz q " +
            "LEFT JOIN FETCH q.lesson l " +
            "LEFT JOIN FETCH l.course c " +
            "WHERE c.id = :courseId")
    List<Quiz> findAllByCourseId(@Param("courseId") Long courseId);

    /** Kiểm tra lesson đã có quiz chưa — enforce 1 lesson = 1 quiz. */
    boolean existsByLessonId(Long lessonId);
}