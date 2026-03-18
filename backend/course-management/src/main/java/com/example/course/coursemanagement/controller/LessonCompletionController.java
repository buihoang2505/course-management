package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.dto.ProgressDTO;
import com.example.course.coursemanagement.repository.UserRepository;
import com.example.course.coursemanagement.service.LessonCompletionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class LessonCompletionController {

    private final LessonCompletionService service;
    private final UserRepository          userRepository;

    // GET /api/progress?userId=1&courseId=2
    @GetMapping
    public ResponseEntity<ProgressDTO> getProgress(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        return ResponseEntity.ok(service.getProgress(userId, courseId));
    }

    // POST /api/progress/complete?lessonId=3  — userId từ JWT
    @PostMapping("/complete")
    public ResponseEntity<?> markComplete(
            @RequestParam Long lessonId,
            @AuthenticationPrincipal UserDetails principal) {
        try {
            Long userId = resolveUserId(principal);
            ProgressDTO progress = service.markComplete(userId, lessonId);
            return ResponseEntity.ok(progress);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/progress/complete?lessonId=3  — userId từ JWT
    @DeleteMapping("/complete")
    public ResponseEntity<?> unmarkComplete(
            @RequestParam Long lessonId,
            @AuthenticationPrincipal UserDetails principal) {
        try {
            Long userId = resolveUserId(principal);
            ProgressDTO progress = service.unmarkComplete(userId, lessonId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"))
                .getId();
    }
}