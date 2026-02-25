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

    // POST /api/grades/assign – nhập điểm cho 1 enrollment
    @PostMapping("/assign")
    public ResponseEntity<Grade> assignGrade(@RequestBody Map<String, Object> body) {
        Long enrollmentId = Long.valueOf(body.get("enrollmentId").toString());
        Double score = Double.valueOf(body.get("score").toString());
        String feedback = body.getOrDefault("feedback", "").toString();
        return ResponseEntity.ok(gradeService.assignGrade(enrollmentId, score, feedback));
    }

    // GET /api/grades/enrollment/{enrollmentId} – xem điểm của 1 enrollment
    @GetMapping("/enrollment/{enrollmentId}")
    public ResponseEntity<Grade> getGradeByEnrollment(@PathVariable Long enrollmentId) {
        return ResponseEntity.ok(gradeService.getGradeByEnrollment(enrollmentId));
    }

    // GET /api/grades/user/{userId} – xem tất cả điểm của 1 user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Grade>> getUserGrades(@PathVariable Long userId) {
        return ResponseEntity.ok(gradeService.getUserGrades(userId));
    }

    // GET /api/grades/course/{courseId}/average – điểm trung bình khóa học
    @GetMapping("/course/{courseId}/average")
    public ResponseEntity<Map<String, Double>> getCourseAverage(@PathVariable Long courseId) {
        Double avg = gradeService.getCourseAverageScore(courseId);
        return ResponseEntity.ok(Map.of("averageScore", avg != null ? avg : 0.0));
    }
}