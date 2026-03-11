package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.Enrollment;
import com.example.course.coursemanagement.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Kiểm tra đã đăng ký chưa
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    // Tìm enrollment cụ thể
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    // Lấy enrollments của user kèm course
    @Query("SELECT e FROM Enrollment e LEFT JOIN FETCH e.course WHERE e.user.id = :userId")
    List<Enrollment> findByUserIdWithCourse(@Param("userId") Long userId);

    // Lấy enrollments của course kèm user
    @Query("SELECT e FROM Enrollment e LEFT JOIN FETCH e.user WHERE e.course.id = :courseId")
    List<Enrollment> findByCourseIdWithUser(@Param("courseId") Long courseId);

    // Lấy enrollments của course kèm user VÀ grade
    @Query("SELECT e FROM Enrollment e " +
            "LEFT JOIN FETCH e.user " +
            "LEFT JOIN FETCH e.grade " +
            "WHERE e.course.id = :courseId")
    List<Enrollment> findByCourseIdWithUserAndGrade(@Param("courseId") Long courseId);

    // Đếm số học viên của 1 khóa học
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);

    // Xóa tất cả enrollment của user (dùng khi xóa user)
    @Modifying
    @Query("DELETE FROM Enrollment e WHERE e.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    // Xóa enrollment của user theo role — dùng để dọn dẹp enrollment của ADMIN
    @Modifying
    @Query("DELETE FROM Enrollment e WHERE e.user.id IN " +
            "(SELECT u.id FROM User u WHERE u.role = :role)")
    void deleteByUserRole(@Param("role") Role role);
}