package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.dto.*;
import com.example.course.coursemanagement.repository.UserRepository;
import com.example.course.coursemanagement.service.QuizService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService      quizService;
    private final UserRepository   userRepository;

    // ════════════════════════════════════════════════════
    //  ADMIN — DANH SÁCH QUIZ
    // ════════════════════════════════════════════════════

    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @GetMapping("/admin/course/{courseId}")
    public ResponseEntity<?> getQuizzesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(quizService.getQuizzesByCourse(courseId));
    }

    @GetMapping("/{id}/admin/detail")
    public ResponseEntity<?> getQuizDetail(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(quizService.getQuizDetail(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════
    //  ADMIN — CRUD QUIZ
    // ════════════════════════════════════════════════════

    @PostMapping
    public ResponseEntity<?> createQuiz(@RequestParam Long lessonId,
                                        @Valid @RequestBody CreateQuizRequest req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(quizService.createQuiz(lessonId, req));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuiz(@PathVariable Long id,
                                        @Valid @RequestBody CreateQuizRequest req) {
        try {
            return ResponseEntity.ok(quizService.updateQuiz(id, req));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long id) {
        try {
            quizService.deleteQuiz(id);
            return ResponseEntity.ok(Map.of("message", "Xoa quiz thanh cong"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════
    //  ADMIN — QUESTION CRUD
    // ════════════════════════════════════════════════════

    @PostMapping("/{quizId}/questions")
    public ResponseEntity<?> addQuestion(@PathVariable Long quizId,
                                         @Valid @RequestBody AddQuestionRequest req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(quizService.addQuestion(quizId, req));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/questions/{questionId}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long questionId,
                                            @Valid @RequestBody AddQuestionRequest req) {
        try {
            return ResponseEntity.ok(quizService.updateQuestion(questionId, req));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        try {
            quizService.deleteQuestion(questionId);
            return ResponseEntity.ok(Map.of("message", "Da xoa cau hoi"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════
    //  STUDENT — XEM QUIZ META
    // ════════════════════════════════════════════════════

    @GetMapping("/lesson/{lessonId}/meta")
    public ResponseEntity<?> getQuizMeta(@PathVariable Long lessonId) {
        QuizMetaDTO meta = quizService.getQuizMetaForLesson(lessonId);
        if (meta == null) return ResponseEntity.ok(Map.of("hasQuiz", false));
        return ResponseEntity.ok(meta);
    }

    // GET /api/quizzes/lesson/{lessonId}/start — userId từ JWT
    @GetMapping("/lesson/{lessonId}/start")
    public ResponseEntity<?> startQuiz(@PathVariable Long lessonId,
                                       @AuthenticationPrincipal UserDetails principal) {
        try {
            Long userId = resolveUserId(principal);
            return ResponseEntity.ok(quizService.getQuizForStudent(lessonId, userId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "outOfAttempts", true));
        }
    }

    // ════════════════════════════════════════════════════
    //  STUDENT — NỘP BÀI
    // ════════════════════════════════════════════════════

    // POST /api/quizzes/{quizId}/submit — userId từ JWT, không tin body
    @PostMapping("/{quizId}/submit")
    public ResponseEntity<?> submitQuiz(@PathVariable Long quizId,
                                        @Valid @RequestBody SubmitQuizRequest req,
                                        @AuthenticationPrincipal UserDetails principal) {
        try {
            Long userId = resolveUserId(principal);
            req.setUserId(userId); // Override userId từ JWT
            return ResponseEntity.ok(quizService.submitQuiz(quizId, req));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Loi cham diem: " + e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════
    //  STUDENT — LỊCH SỬ
    // ════════════════════════════════════════════════════

    @GetMapping("/{quizId}/attempts")
    public ResponseEntity<?> getAttemptHistory(@PathVariable Long quizId,
                                               @RequestParam Long userId) {
        return ResponseEntity.ok(quizService.getAttemptHistory(quizId, userId));
    }

    // GET result — chỉ chính chủ hoặc ADMIN
    @GetMapping("/attempts/{attemptId}/result")
    public ResponseEntity<?> getAttemptResult(@PathVariable Long attemptId,
                                              @AuthenticationPrincipal UserDetails principal) {
        try {
            Long userId  = resolveUserId(principal);
            boolean isAdmin = principal.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            return ResponseEntity.ok(quizService.getAttemptResult(attemptId, userId, isAdmin));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════
    //  ADMIN — THỐNG KÊ
    // ════════════════════════════════════════════════════

    @GetMapping("/{quizId}/stats")
    public ResponseEntity<?> getStats(@PathVariable Long quizId) {
        try {
            return ResponseEntity.ok(quizService.getStats(quizId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════
    //  HELPER
    // ════════════════════════════════════════════════════

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"))
                .getId();
    }
}