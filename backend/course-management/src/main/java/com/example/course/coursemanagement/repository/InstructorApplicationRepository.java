package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.InstructorApplication;
import com.example.course.coursemanagement.entity.InstructorApplication.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InstructorApplicationRepository extends JpaRepository<InstructorApplication, Long> {

    /** Đơn mới nhất của user */
    Optional<InstructorApplication> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    /** Đang có đơn PENDING? */
    boolean existsByUserIdAndStatus(Long userId, Status status);

    /** Bị REJECT trong 30 ngày gần đây? (anti-spam) */
    @Query("""
        SELECT COUNT(a) > 0 FROM InstructorApplication a
        WHERE a.user.id = :userId
          AND a.status = 'REJECTED'
          AND a.reviewedAt > :since
    """)
    boolean hasRecentRejection(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /** Admin: toàn bộ danh sách mới nhất trước */
    List<InstructorApplication> findAllByOrderByCreatedAtDesc();

    /** Admin: lọc theo status */
    List<InstructorApplication> findByStatusOrderByCreatedAtDesc(Status status);

    @Modifying
    @Query("DELETE FROM InstructorApplication a WHERE a.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}