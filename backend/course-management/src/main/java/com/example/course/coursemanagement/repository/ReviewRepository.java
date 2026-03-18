package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Lấy tất cả review của 1 khóa học, mới nhất trước
    @Query("SELECT r FROM Review r JOIN FETCH r.user u LEFT JOIN FETCH u.profile WHERE r.course.id = :courseId ORDER BY r.createdAt DESC")
    List<Review> findByCourseIdWithUser(@Param("courseId") Long courseId);

    // Kiểm tra user đã review chưa (fetch profile để build DTO)
    @Query("SELECT r FROM Review r JOIN FETCH r.user u LEFT JOIN FETCH u.profile JOIN FETCH r.course WHERE r.user.id = :userId AND r.course.id = :courseId")
    Optional<Review> findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    // Lấy 1 review kèm đầy đủ relations
    @Query("SELECT r FROM Review r JOIN FETCH r.user u LEFT JOIN FETCH u.profile JOIN FETCH r.course WHERE r.id = :id")
    Optional<Review> findByIdWithDetails(@Param("id") Long id);

    // Đếm số review và tính avg rating của 1 khóa học
    @Query("SELECT COUNT(r), AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
    Object[] countAndAvgByCourseId(@Param("courseId") Long courseId);

    // Tất cả review (admin)
    @Query("SELECT r FROM Review r JOIN FETCH r.user u LEFT JOIN FETCH u.profile JOIN FETCH r.course ORDER BY r.createdAt DESC")
    List<Review> findAllWithDetails();

    // Lấy chỉ rating values — an toàn, không lazy load entity
    @Query("SELECT r.rating FROM Review r WHERE r.course.id = :courseId")
    List<Integer> findRatingsByCourseId(@Param("courseId") Long courseId);

    // Xóa theo courseId
    void deleteByCourseId(Long courseId);

    /** Batch: COUNT+AVG rating cho tất cả courses — fix N+1 */
    @Query("SELECT r.course.id, COUNT(r), AVG(r.rating) FROM Review r GROUP BY r.course.id")
    List<Object[]> countAndAvgGroupedByCourse();

    @Modifying
    @Query("DELETE FROM Review r WHERE r.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}