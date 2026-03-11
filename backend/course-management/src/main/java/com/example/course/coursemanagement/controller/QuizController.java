package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.dto.*;
import com.example.course.coursemanagement.service.QuizService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // ════════════════════════════════════════════════════
    //  ADMIN — DANH SÁCH QUIZ
    // ════════════════════════════════════════════════════

    /** GET /api/quizzes/admin/all — tất cả quiz kèm lesson + course */
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    /** GET /api/quizzes/admin/course/{courseId} — quiz theo khóa học */
    @GetMapping("/admin/course/{courseId}")
    public ResponseEntity<?> getQuizzesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(quizService.getQuizzesByCourse(courseId));
    }

    /**
     * GET /api/quizzes/{id}/admin/detail
     * Lấy quiz đầy đủ cho admin edit: questions + choices + isCorrect
     */
    @GetMapping("/{id}/admin/detail")
    public ResponseEntity<?> getQuizDetail(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(quizService.getQuizDetail(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════
    //  ADMIN — CRUD QUIZ
    // ════════════════════════════════════════════════════

    /**
     * POST /api/quizzes?lessonId=X
     * Body: { title, description, passingScore, timeLimitMinutes, maxAttempts, isActive }
     */
    @PostMapping
    public ResponseEntity<?> createQuiz(@RequestParam Long lessonId,
                                        @Valid @RequestBody CreateQuizRequest req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(quizService.createQuiz(lessonId, req));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /api/quizzes/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuiz(@PathVariable Long id,
                                        @Valid @RequestBody CreateQuizRequest req) {
        try {
            return ResponseEntity.ok(quizService.updateQuiz(id, req));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** DELETE /api/quizzes/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long id) {
        try {
            quizService.deleteQuiz(id);
            return ResponseEntity.ok(Map.of("message", "Xóa quiz thành công"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════
    //  ADMIN — QUESTION CRUD
    // ════════════════════════════════════════════════════

    /**
     * POST /api/quizzes/{quizId}/questions
     * Body: { content, explanation, points, choices: [{content, isCorrect}] }
     */
    @PostMapping("/{quizId}/questions")
    public ResponseEntity<?> addQuestion(@PathVariable Long quizId,
                                         @Valid @RequestBody AddQuestionRequest req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(quizService.addQuestion(quizId, req));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /api/quizzes/questions/{questionId} */
    @PutMapping("/questions/{questionId}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long questionId,
                                            @Valid @RequestBody AddQuestionRequest req) {
        try {
            return ResponseEntity.ok(quizService.updateQuestion(questionId, req));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** DELETE /api/quizzes/questions/{questionId} */
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        try {
            quizService.deleteQuestion(questionId);
            return ResponseEntity.ok(Map.of("message", "Đã xóa câu hỏi"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════
    //  STUDENT — XEM QUIZ
    // ════════════════════════════════════════════════════

    /**
     * GET /api/quizzes/lesson/{lessonId}/meta
     * Trả về METADATA quiz (không có câu hỏi) để hiển thị block quiz trong accordion.
     * LUÔN trả 200 (null body nếu không có quiz) — không bao giờ trả 400/404.
     */
    @GetMapping("/lesson/{lessonId}/meta")
    public ResponseEntity<?> getQuizMeta(@PathVariable Long lessonId) {
        QuizMetaDTO meta = quizService.getQuizMetaForLesson(lessonId);
        if (meta == null) {
            return ResponseEntity.ok(Map.of("hasQuiz", false));
        }
        return ResponseEntity.ok(meta);
    }

    /**
     * GET /api/quizzes/lesson/{lessonId}/start?userId=X
     * Lấy FULL quiz để bắt đầu làm bài (có câu hỏi, không có đáp án đúng).
     * Trả 400 nếu hết lượt, 404 nếu không có quiz.
     */
    @GetMapping("/lesson/{lessonId}/start")
    public ResponseEntity<?> startQuiz(@PathVariable Long lessonId,
                                       @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(quizService.getQuizForStudent(lessonId, userId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage(), "outOfAttempts", true));
        }
    }

    // ════════════════════════════════════════════════════
    //  STUDENT — NỘP BÀI
    // ════════════════════════════════════════════════════

    /** POST /api/quizzes/{quizId}/submit */
    @PostMapping("/{quizId}/submit")
    public ResponseEntity<?> submitQuiz(@PathVariable Long quizId,
                                        @Valid @RequestBody SubmitQuizRequest req) {
        try {
            return ResponseEntity.ok(quizService.submitQuiz(quizId, req));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Lỗi chấm điểm: " + e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════
    //  STUDENT — LỊCH SỬ
    // ════════════════════════════════════════════════════

    /** GET /api/quizzes/{quizId}/attempts?userId=X */
    @GetMapping("/{quizId}/attempts")
    public ResponseEntity<?> getAttemptHistory(@PathVariable Long quizId,
                                               @RequestParam Long userId) {
        return ResponseEntity.ok(quizService.getAttemptHistory(quizId, userId));
    }

    /** GET /api/quizzes/attempts/{attemptId}/result */
    @GetMapping("/attempts/{attemptId}/result")
    public ResponseEntity<?> getAttemptResult(@PathVariable Long attemptId) {
        try {
            return ResponseEntity.ok(quizService.getAttemptResult(attemptId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ════════════════════════════════════════════════════
    //  ADMIN — THỐNG KÊ
    // ════════════════════════════════════════════════════

    /** GET /api/quizzes/{quizId}/stats */
    @GetMapping("/{quizId}/stats")
    public ResponseEntity<?> getStats(@PathVariable Long quizId) {
        try {
            return ResponseEntity.ok(quizService.getStats(quizId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}