package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.entity.*;
import com.example.course.coursemanagement.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class LessonQAController {

    private final LessonQuestionRepository questionRepo;
    private final QuestionReplyRepository  replyRepo;
    private final UserRepository           userRepo;
    private final LessonRepository         lessonRepo;

    private User resolveUser(UserDetails p) {
        return userRepo.findByUsername(p.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    // ── Helper: load questions by IDs (giữ thứ tự IDs) ──────────
    @Transactional(readOnly = true)
    List<LessonQuestion> loadByIds(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        // Fetch base data + lesson/course
        List<LessonQuestion> questions = questionRepo.findByIdsWithDetails(ids);
        // Fetch replies trong transaction riêng (tránh MultipleBagFetchException)
        questionRepo.fetchRepliesByIds(ids);
        // Sort theo thứ tự IDs gốc (đã ordered DESC từ query đầu)
        Map<Long, LessonQuestion> map = questions.stream()
                .collect(Collectors.toMap(LessonQuestion::getId, q -> q));
        return ids.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Map<String, Object> qMap(LessonQuestion q) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",        q.getId());
        m.put("content",   q.getContent());
        m.put("resolved",  q.getResolved());
        m.put("createdAt", q.getCreatedAt() != null ? q.getCreatedAt().toString() : null);
        m.put("authorId",  q.getUser().getId());
        m.put("author",    q.getUser().getUsername());
        m.put("authorAvatar", q.getUser().getProfile() != null ? q.getUser().getProfile().getAvatar() : null);
        if (q.getLesson() != null) {
            m.put("lessonId",    q.getLesson().getId());
            m.put("lessonTitle", q.getLesson().getTitle());
            if (q.getLesson().getCourse() != null) {
                m.put("courseId",    q.getLesson().getCourse().getId());
                m.put("courseTitle", q.getLesson().getCourse().getTitle());
            }
        }
        m.put("replies", q.getReplies() == null ? List.of()
                : q.getReplies().stream().map(this::rMap).collect(Collectors.toList()));
        return m;
    }

    private Map<String, Object> rMap(QuestionReply r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",        r.getId());
        m.put("content",   r.getContent());
        m.put("official",  r.getOfficial());
        m.put("role",      r.getUser().getRole().name());
        m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
        m.put("authorId",  r.getUser().getId());
        m.put("author",    r.getUser().getUsername());
        m.put("authorAvatar", r.getUser().getProfile() != null ? r.getUser().getProfile().getAvatar() : null);
        return m;
    }

    // GET /api/lessons/{lessonId}/questions
    @Transactional(readOnly = true)
    @GetMapping("/api/lessons/{lessonId}/questions")
    public ResponseEntity<List<Map<String, Object>>> listByLesson(@PathVariable Long lessonId) {
        List<Long> ids = questionRepo.findIdsByLessonId(lessonId);
        return ResponseEntity.ok(loadByIds(ids).stream().map(this::qMap).collect(Collectors.toList()));
    }

    // GET /api/instructor/questions/inbox?resolved=false
    @Transactional(readOnly = true)
    @GetMapping("/api/instructor/questions/inbox")
    public ResponseEntity<List<Map<String, Object>>> instructorInbox(
            @RequestParam(defaultValue = "false") boolean resolved,
            @AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        List<Long> ids = questionRepo.findIdsByInstructorUsername(user.getUsername(), resolved);
        return ResponseEntity.ok(loadByIds(ids).stream().map(this::qMap).collect(Collectors.toList()));
    }

    // GET /api/admin/questions?resolved=false|true (optional)
    @Transactional(readOnly = true)
    @GetMapping("/api/admin/questions")
    public ResponseEntity<List<Map<String, Object>>> adminAll(
            @RequestParam(required = false) Boolean resolved) {
        List<Long> ids = (resolved != null)
                ? questionRepo.findIdsByResolved(resolved)
                : questionRepo.findAllIds();
        return ResponseEntity.ok(loadByIds(ids).stream().map(this::qMap).collect(Collectors.toList()));
    }

    // POST /api/lessons/{lessonId}/questions
    @PostMapping("/api/lessons/{lessonId}/questions")
    public ResponseEntity<?> ask(@PathVariable Long lessonId,
                                 @RequestBody Map<String, String> body,
                                 @AuthenticationPrincipal UserDetails principal) {
        String content = body.getOrDefault("content", "").trim();
        if (content.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Noi dung khong duoc de trong!"));
        if (content.length() > 2000)
            return ResponseEntity.badRequest().body(Map.of("error", "Toi da 2000 ky tu"));
        Lesson lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Khong tim thay bai hoc"));
        User user = resolveUser(principal);
        LessonQuestion q = LessonQuestion.builder()
                .lesson(lesson).user(user).content(content).build();
        questionRepo.save(q);
        return ResponseEntity.ok(qMap(q));
    }

    // POST /api/questions/{questionId}/replies
    @PostMapping("/api/questions/{questionId}/replies")
    public ResponseEntity<?> reply(@PathVariable Long questionId,
                                   @RequestBody Map<String, String> body,
                                   @AuthenticationPrincipal UserDetails principal) {
        String content = body.getOrDefault("content", "").trim();
        if (content.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Noi dung khong duoc de trong!"));
        LessonQuestion q = questionRepo.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Khong tim thay cau hoi"));
        User user = resolveUser(principal);
        boolean isOfficial = user.getRole() == Role.INSTRUCTOR || user.getRole() == Role.ADMIN;
        QuestionReply r = QuestionReply.builder()
                .question(q).user(user).content(content).official(isOfficial).build();
        replyRepo.save(r);
        if (isOfficial && !Boolean.TRUE.equals(q.getResolved())) {
            q.setResolved(true);
            questionRepo.save(q);
        }
        return ResponseEntity.ok(rMap(r));
    }

    // PATCH /api/questions/{questionId}/resolve
    @PatchMapping("/api/questions/{questionId}/resolve")
    public ResponseEntity<?> toggleResolve(@PathVariable Long questionId,
                                           @AuthenticationPrincipal UserDetails principal) {
        LessonQuestion q = questionRepo.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Khong tim thay cau hoi"));
        User user = resolveUser(principal);
        boolean canAct = q.getUser().getId().equals(user.getId())
                || user.getRole() == Role.ADMIN || user.getRole() == Role.INSTRUCTOR;
        if (!canAct) return ResponseEntity.status(403).body(Map.of("error", "Khong co quyen!"));
        q.setResolved(!Boolean.TRUE.equals(q.getResolved()));
        questionRepo.save(q);
        return ResponseEntity.ok(Map.of("resolved", q.getResolved()));
    }

    // DELETE /api/questions/{questionId}
    @DeleteMapping("/api/questions/{questionId}")
    public ResponseEntity<?> deleteQ(@PathVariable Long questionId,
                                     @AuthenticationPrincipal UserDetails principal) {
        LessonQuestion q = questionRepo.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Khong tim thay cau hoi"));
        User user = resolveUser(principal);
        if (!q.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN)
            return ResponseEntity.status(403).body(Map.of("error", "Khong co quyen xoa!"));
        questionRepo.deleteById(questionId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // DELETE /api/replies/{replyId}
    @DeleteMapping("/api/replies/{replyId}")
    public ResponseEntity<?> deleteR(@PathVariable Long replyId,
                                     @AuthenticationPrincipal UserDetails principal) {
        QuestionReply r = replyRepo.findById(replyId)
                .orElseThrow(() -> new EntityNotFoundException("Khong tim thay cau tra loi"));
        User user = resolveUser(principal);
        if (!r.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN)
            return ResponseEntity.status(403).body(Map.of("error", "Khong co quyen xoa!"));
        replyRepo.deleteById(replyId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}