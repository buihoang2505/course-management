package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.dto.ReviewDTO;
import com.example.course.coursemanagement.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // GET /api/reviews/course/{courseId} — danh sách review của 1 khóa học
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ReviewDTO>> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(reviewService.getByCourse(courseId));
    }

    // GET /api/reviews/course/{courseId}/stats — thống kê rating
    @GetMapping("/course/{courseId}/stats")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable Long courseId) {
        return ResponseEntity.ok(reviewService.getStats(courseId));
    }

    // GET /api/reviews/my?userId=&courseId= — review của user cho khóa học
    @GetMapping("/my")
    public ResponseEntity<ReviewDTO> getMyReview(@RequestParam Long userId,
                                                 @RequestParam Long courseId) {
        return ResponseEntity.ok(reviewService.getMyReview(userId, courseId));
    }

    // POST /api/reviews — tạo hoặc cập nhật review
    @PostMapping
    public ResponseEntity<?> upsert(@RequestBody Map<String, Object> body) {
        try {
            Long   userId   = Long.parseLong(body.get("userId").toString());
            Long   courseId = Long.parseLong(body.get("courseId").toString());
            int    rating   = Integer.parseInt(body.get("rating").toString());
            String comment  = body.get("comment") != null ? body.get("comment").toString() : null;
            return ResponseEntity.ok(reviewService.upsert(userId, courseId, rating, comment));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/reviews/{id}?userId=&admin=
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @RequestParam Long userId,
                                    @RequestParam(defaultValue = "false") boolean admin) {
        try {
            reviewService.delete(id, userId, admin);
            return ResponseEntity.ok(Map.of("message", "Đã xóa review!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/reviews/admin/all — admin xem tất cả (dùng trong AdminView)
    @GetMapping("/admin/all")
    public ResponseEntity<List<ReviewDTO>> getAll() {
        return ResponseEntity.ok(reviewService.getAll());
    }
}