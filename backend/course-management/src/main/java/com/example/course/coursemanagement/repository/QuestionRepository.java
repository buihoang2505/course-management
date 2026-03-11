package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizIdOrderByOrderNumAsc(Long quizId);

    // Đếm câu hỏi trong quiz
    long countByQuizId(Long quizId);

    // Cập nhật lại orderNum sau khi xóa câu
    @Modifying
    @Query("UPDATE Question q SET q.orderNum = q.orderNum - 1 " +
            "WHERE q.quiz.id = :quizId AND q.orderNum > :deletedOrder")
    void shiftOrderNumAfterDelete(@Param("quizId") Long quizId,
                                  @Param("deletedOrder") int deletedOrder);
}