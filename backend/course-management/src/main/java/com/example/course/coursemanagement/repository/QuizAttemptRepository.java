package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    // Lấy lần làm gần nhất của user
    Optional<QuizAttempt> findFirstByQuizIdAndUserIdOrderByStartedAtDesc(
            Long quizId, Long userId);

    // Đếm số lần user đã làm quiz
    long countByQuizIdAndUserId(Long quizId, Long userId);

    // Tất cả lần làm của user với quiz này (dùng để tìm điểm cao nhất)
    List<QuizAttempt> findByQuizIdAndUserId(Long quizId, Long userId);

    // Tất cả lần làm của user với quiz này, sắp theo mới nhất
    List<QuizAttempt> findByQuizIdAndUserIdOrderByStartedAtDesc(Long quizId, Long userId);

    // Lấy attempt kèm answers (tránh N+1 khi xem kết quả)
    @Query("SELECT a FROM QuizAttempt a " +
            "LEFT JOIN FETCH a.answers ans " +
            "LEFT JOIN FETCH ans.question " +
            "LEFT JOIN FETCH ans.choice " +
            "WHERE a.id = :id")
    Optional<QuizAttempt> findByIdWithAnswers(@Param("id") Long id);

    // Thống kê: lấy tất cả attempt của 1 quiz (admin)
    List<QuizAttempt> findByQuizId(Long quizId);

    // Kiểm tra user đã pass quiz chưa
    boolean existsByQuizIdAndUserIdAndPassedTrue(Long quizId, Long userId);
    // Thêm method này vào QuizAttemptRepository.java (đã có sẵn)

    // Lấy lần làm có điểm cao nhất (dùng tính avg certificate)
    Optional<QuizAttempt> findFirstByQuizIdAndUserIdOrderByScoreDesc(Long quizId, Long userId);

    // Dùng khi xóa user — xóa toàn bộ attempt (AttemptAnswer cascade ALL từ attempt)
    @Modifying
    @Query("DELETE FROM QuizAttempt a WHERE a.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}