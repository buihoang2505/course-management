package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTxnRef(String txnRef);

    boolean existsByUserIdAndCourseIdAndStatus(Long userId, Long courseId, Payment.PaymentStatus status);

    @Query("SELECT p FROM Payment p JOIN FETCH p.course JOIN FETCH p.user WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    List<Payment> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.course c JOIN FETCH p.user u ORDER BY p.createdAt DESC")
    List<Payment> findAllWithDetails();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM Payment p WHERE p.course.id = :courseId")
    void deleteByCourseId(@org.springframework.data.repository.query.Param("courseId") Long courseId);
}