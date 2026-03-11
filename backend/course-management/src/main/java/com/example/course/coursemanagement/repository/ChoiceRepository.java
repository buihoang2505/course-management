package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {

    List<Choice> findByQuestionId(Long questionId);

    /**
     * FIX ObjectDeletedException: xóa bằng JPQL thay vì deleteAll(entities).
     * deleteAll(entities) để Hibernate track entity → bị lỗi khi merge sau đó.
     * @Modifying + JPQL thực thi DELETE trực tiếp, Hibernate không track.
     */
    @Modifying
    @Query("DELETE FROM Choice c WHERE c.question.id = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);

    // Reset tất cả về false trước khi set đáp án đúng
    @Modifying
    @Query("UPDATE Choice c SET c.isCorrect = false WHERE c.question.id = :questionId")
    void resetCorrectByQuestionId(@Param("questionId") Long questionId);

    // Đếm đáp án đúng trong 1 câu (validate: phải == 1)
    long countByQuestionIdAndIsCorrectTrue(Long questionId);
}