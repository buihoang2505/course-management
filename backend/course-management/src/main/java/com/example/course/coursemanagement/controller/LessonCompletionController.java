package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.dto.ProgressDTO;
import com.example.course.coursemanagement.service.LessonCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class LessonCompletionController {

    private final LessonCompletionService service;

    // GET /api/progress?userId=1&courseId=2
    // Lấy tiến độ học tập của user trong course
    @GetMapping
    public ResponseEntity<ProgressDTO> getProgress(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        return ResponseEntity.ok(service.getProgress(userId, courseId));
    }

    // POST /api/progress/complete?userId=1&lessonId=3
    // Đánh dấu bài học hoàn thành
    @PostMapping("/complete")
    public ResponseEntity<?> markComplete(
            @RequestParam Long userId,
            @RequestParam Long lessonId) {
        try {
            ProgressDTO progress = service.markComplete(userId, lessonId);
            return ResponseEntity.ok(progress);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/progress/complete?userId=1&lessonId=3
    // Bỏ đánh dấu bài học hoàn thành
    @DeleteMapping("/complete")
    public ResponseEntity<?> unmarkComplete(
            @RequestParam Long userId,
            @RequestParam Long lessonId) {
        try {
            ProgressDTO progress = service.unmarkComplete(userId, lessonId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}