package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    Optional<Certificate> findByUserIdAndCourseId(Long userId, Long courseId);

    // Dùng cho trang Profile và /certificates
    @Query("SELECT c FROM Certificate c " +
            "JOIN FETCH c.course " +
            "WHERE c.user.id = :userId " +
            "ORDER BY c.issuedAt DESC")
    List<Certificate> findByUserIdWithCourse(@Param("userId") Long userId);

    // Verify chứng chỉ public
    Optional<Certificate> findByCertificateCode(String certificateCode);

    // ── ADMIN queries ──────────────────────────────────────────

    /** Lấy tất cả chứng chỉ kèm user + course (admin) */
    @Query("SELECT c FROM Certificate c " +
            "JOIN FETCH c.user u " +
            "JOIN FETCH c.course co " +
            "ORDER BY c.issuedAt DESC")
    List<Certificate> findAllWithUserAndCourse();

    /** Lọc theo courseId (admin) */
    @Query("SELECT c FROM Certificate c " +
            "JOIN FETCH c.user u " +
            "JOIN FETCH c.course co " +
            "WHERE c.course.id = :courseId " +
            "ORDER BY c.issuedAt DESC")
    List<Certificate> findByCourseIdWithUser(@Param("courseId") Long courseId);

    /** Tổng số chứng chỉ đã cấp */
    long count();

    /** Số chứng chỉ theo từng khóa học (thống kê) */
    @Query("SELECT c.courseTitle, COUNT(c) FROM Certificate c GROUP BY c.courseTitle ORDER BY COUNT(c) DESC")
    List<Object[]> countByCourseTitle();

    // Dùng khi xóa user
    void deleteByUserId(Long userId);
}