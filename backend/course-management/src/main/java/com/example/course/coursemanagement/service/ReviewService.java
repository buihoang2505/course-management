package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.dto.ReviewDTO;
import com.example.course.coursemanagement.entity.Course;
import com.example.course.coursemanagement.entity.Enrollment;
import com.example.course.coursemanagement.entity.Review;
import com.example.course.coursemanagement.entity.User;
import com.example.course.coursemanagement.repository.EnrollmentRepository;
import com.example.course.coursemanagement.repository.ReviewRepository;
import com.example.course.coursemanagement.repository.CourseRepository;
import com.example.course.coursemanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final UserRepository   userRepo;
    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollRepo;

    // ── Lấy review của 1 khóa học ──────────────────────────
    @Transactional(readOnly = true)
    public List<ReviewDTO> getByCourse(Long courseId) {
        return reviewRepo.findByCourseIdWithUser(courseId)
                .stream().map(ReviewDTO::from).collect(Collectors.toList());
    }

    // ── Thống kê rating của 1 khóa học ─────────────────────
    @Transactional(readOnly = true)
    public Map<String, Object> getStats(Long courseId) {
        Object[] res = reviewRepo.countAndAvgByCourseId(courseId);
        long count  = res[0] != null ? ((Number) res[0]).longValue() : 0;
        double avg  = res[1] != null ? ((Number) res[1]).doubleValue() : 0.0;

        // Phân bổ từng sao
        List<Review> reviews = reviewRepo.findByCourseIdWithUser(courseId);
        Map<Integer, Long> dist = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            int star = i;
            dist.put(i, reviews.stream().filter(r -> r.getRating() == star).count());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalReviews", count);
        result.put("avgRating", Math.round(avg * 10.0) / 10.0);
        result.put("distribution", dist);
        return result;
    }

    // ── Tạo hoặc cập nhật review ────────────────────────────
    public ReviewDTO upsert(Long userId, Long courseId, int rating, String comment) {
        // Validate rating
        if (rating < 1 || rating > 5)
            throw new IllegalArgumentException("Rating phải từ 1 đến 5!");

        // Kiểm tra đã enroll chưa
        enrollRepo.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new IllegalStateException("Bạn chưa đăng ký khóa học này!"));

        User   user   = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        // Upsert: tạo mới hoặc cập nhật
        Review review = reviewRepo.findByUserIdAndCourseId(userId, courseId)
                .orElseGet(() -> {
                    Review r = new Review();
                    r.setUser(user);
                    r.setCourse(course);
                    return r;
                });

        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : null);
        Review saved = reviewRepo.save(review);

        // Reload với JOIN FETCH user + profile để build DTO đúng
        return ReviewDTO.from(reviewRepo.findByIdWithDetails(saved.getId()).get());
    }

    // ── Xóa review (user tự xóa hoặc admin) ────────────────
    public void delete(Long reviewId, Long requestUserId, boolean isAdmin) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        if (!isAdmin && !review.getUser().getId().equals(requestUserId))
            throw new IllegalStateException("Không có quyền xóa review này!");
        reviewRepo.delete(review);
    }

    // ── Lấy review của user cho 1 khóa học ─────────────────
    @Transactional(readOnly = true)
    public ReviewDTO getMyReview(Long userId, Long courseId) {
        return reviewRepo.findByUserIdAndCourseId(userId, courseId)
                .map(ReviewDTO::from).orElse(null);
    }

    // ── Admin: lấy tất cả review ────────────────────────────
    @Transactional(readOnly = true)
    public List<ReviewDTO> getAll() {
        return reviewRepo.findAllWithDetails()
                .stream().map(ReviewDTO::from).collect(Collectors.toList());
    }
}