package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId")
    void markAllReadByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    // Xóa thông báo cũ hơn X ngày (dùng cho scheduled cleanup)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") java.time.LocalDateTime cutoff);

    // Đếm thông báo cũ hơn X ngày
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.createdAt < :cutoff")
    long countOlderThan(@Param("cutoff") java.time.LocalDateTime cutoff);

    // Admin pagination queries
    @Query("SELECT n FROM Notification n WHERE CAST(n.type AS string) = :type ORDER BY n.createdAt DESC")
    Page<Notification> findByTypeContaining(@Param("type") String type, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE LOWER(n.title) LIKE %:q% OR LOWER(n.message) LIKE %:q% ORDER BY n.createdAt DESC")
    Page<Notification> findBySearch(@Param("q") String q, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE CAST(n.type AS string) = :type AND (LOWER(n.title) LIKE %:q% OR LOWER(n.message) LIKE %:q%) ORDER BY n.createdAt DESC")
    Page<Notification> findByTypeAndSearch(@Param("type") String type, @Param("q") String q, Pageable pageable);
}