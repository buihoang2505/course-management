package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.entity.Grade;
import com.example.course.coursemanagement.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    // ======================================================
    // POST – Tính điểm TỰ ĐỘNG dựa trên tiến độ bài học
    // Body: { "enrollmentId": 1 }
    // ======================================================
    @PostMapping("/assign")
    public ResponseEntity<?> assignGrade(@RequestBody Map<String, Object> body) {
        try {
            Long enrollmentId = Long.valueOf(body.get("enrollmentId").toString());

            // Nếu có score trong body → nhập thủ công; không có → tính tự động
            if (body.containsKey("score") && body.get("score") != null
                    && !body.get("score").toString().isBlank()) {

                Double score    = Double.valueOf(body.get("score").toString());
                String feedback = body.getOrDefault("feedback", "").toString();
                return ResponseEntity.ok(gradeService.assignGradeManual(enrollmentId, score, feedback));

            } else {
                // Tính tự động
                return ResponseEntity.ok(gradeService.calculateAutoGrade(enrollmentId));
            }

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ======================================================
    // POST – Tính điểm tự động theo enrollmentId (explicit)
    // ======================================================
    @PostMapping("/calculate/{enrollmentId}")
    public ResponseEntity<?> calculateGrade(@PathVariable Long enrollmentId) {
        try {
            return ResponseEntity.ok(gradeService.calculateAutoGrade(enrollmentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ======================================================
    // GET – Xem điểm theo enrollment
    // ======================================================
    @GetMapping("/enrollment/{enrollmentId}")
    public ResponseEntity<?> getGradeByEnrollment(@PathVariable Long enrollmentId) {
        try {
            return ResponseEntity.ok(gradeService.getGradeByEnrollment(enrollmentId));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // ======================================================
    // GET – Xem tất cả điểm của user
    // ======================================================
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Grade>> getUserGrades(@PathVariable Long userId) {
        return ResponseEntity.ok(gradeService.getUserGrades(userId));
    }

    // ======================================================
    // GET – Điểm trung bình khóa học
    // ======================================================
    @GetMapping("/course/{courseId}/average")
    public ResponseEntity<Map<String, Double>> getCourseAverage(@PathVariable Long courseId) {
        Double avg = gradeService.getCourseAverageScore(courseId);
        return ResponseEntity.ok(Map.of("averageScore", avg != null ? avg : 0.0));
    }
}