package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.EmailQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailQueueRepository extends JpaRepository<EmailQueue, Long> {

    /**
     * Lấy batch PENDING sẵn sàng gửi (scheduledAt <= now hoặc null).
     * Giới hạn 20 email/lần để tránh spam Gmail.
     */
    @Query("""
        SELECT e FROM EmailQueue e
        WHERE e.status = 'PENDING'
          AND (e.scheduledAt IS NULL OR e.scheduledAt <= :now)
        ORDER BY e.createdAt ASC
        LIMIT 20
        """)
    List<EmailQueue> findPendingBatch(@Param("now") LocalDateTime now);

    /** Đổi trạng thái hàng loạt → SENDING để tránh double-send */
    @Modifying
    @Query("UPDATE EmailQueue e SET e.status = 'SENDING' WHERE e.id IN :ids")
    void markAsSending(@Param("ids") List<Long> ids);

    /** Thống kê theo status (admin) */
    long countByStatus(EmailQueue.Status status);

    /** Xóa email đã gửi thành công quá 30 ngày (cleanup) */
    @Modifying
    @Query("DELETE FROM EmailQueue e WHERE e.status = 'SENT' AND e.sentAt < :before")
    void deleteOldSent(@Param("before") LocalDateTime before);

    /** Kiểm tra đã có reminder hôm nay cho email này chưa (tránh gửi 2 lần) */
    @Query("""
        SELECT COUNT(e) > 0 FROM EmailQueue e
        WHERE e.toEmail = :email
          AND e.emailType = 'REMINDER'
          AND e.createdAt >= :startOfDay
        """)
    boolean hasReminderTodayFor(@Param("email") String email,
                                @Param("startOfDay") LocalDateTime startOfDay);
}