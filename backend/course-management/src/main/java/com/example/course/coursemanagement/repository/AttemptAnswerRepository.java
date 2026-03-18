package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.AttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {

    List<AttemptAnswer> findByAttemptId(Long attemptId);

    /**
     * Nullify choice_id trong attempt_answers trước khi xóa choices.
     * Dùng khi admin update câu hỏi (xóa choices cũ, tạo choices mới).
     * SET NULL thay vì DELETE để giữ lịch sử làm bài.
     */
    /** Xóa answers thuộc danh sách attemptIds — gọi trước khi xóa quiz_attempts */
    @Modifying
    @Query("DELETE FROM AttemptAnswer aa WHERE aa.attempt.id IN :attemptIds")
    void deleteByAttemptIds(@Param("attemptIds") List<Long> attemptIds);

    @Modifying
    @Query("UPDATE AttemptAnswer aa SET aa.choice = null WHERE aa.question.id = :questionId")
    void nullifyChoiceByQuestionId(@Param("questionId") Long questionId);

    // Thống kê: tỉ lệ đúng theo từng câu hỏi (dùng trong QuizStats)
    @Query("SELECT aa.question.id, COUNT(aa), SUM(CASE WHEN aa.isCorrect = true THEN 1 ELSE 0 END) " +
            "FROM AttemptAnswer aa " +
            "WHERE aa.question.quiz.id = :quizId " +
            "GROUP BY aa.question.id")
    List<Object[]> getCorrectRateByQuestion(@Param("quizId") Long quizId);
}