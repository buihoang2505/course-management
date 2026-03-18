package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.dto.InstructorApplicationDTO;
import com.example.course.coursemanagement.entity.InstructorApplication;
import com.example.course.coursemanagement.service.InstructorApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class InstructorApplicationController {

    private final InstructorApplicationService svc;

    // ── Student: nộp đơn ─────────────────────────────────────────────────
    // POST /api/applications/instructor/apply
    @PostMapping("/api/applications/instructor/apply")
    public ResponseEntity<?> apply(@RequestBody Map<String, String> body) {
        try {
            Long userId = Long.parseLong(body.get("userId"));
            String exp      = body.get("experience");
            String expertise = body.get("expertise");
            String portfolio = body.get("portfolioUrl");

            if (exp == null || exp.isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "Kinh nghiệm không được để trống!"));

            InstructorApplication saved = svc.apply(userId, exp, expertise, portfolio);
            return ResponseEntity.ok(Map.of(
                    "message", "Nộp đơn thành công! Admin sẽ xem xét sớm nhất.",
                    "id",      saved.getId(),
                    "status",  saved.getStatus().name()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Student: xem đơn của mình ────────────────────────────────────────
    // GET /api/applications/instructor/my?userId=1
    @GetMapping("/api/applications/instructor/my")
    public ResponseEntity<?> myApp(@RequestParam Long userId) {
        Map<String, Object> app = svc.getMyApplication(userId);
        if (app == null) return ResponseEntity.ok(Map.of("exists", false));
        return ResponseEntity.ok(app);
    }

    // ── Admin: danh sách đơn ─────────────────────────────────────────────
    // GET /api/admin/instructor-applications?status=PENDING
    @GetMapping("/api/admin/instructor-applications")
    public ResponseEntity<List<InstructorApplicationDTO>> list(
            @RequestParam(required = false) String status) {
        List<InstructorApplication> data = (status != null && !status.isBlank())
                ? svc.getByStatus(status)
                : svc.getAll();
        return ResponseEntity.ok(
                data.stream().map(InstructorApplicationDTO::from).collect(Collectors.toList()));
    }

    // ── Admin: duyệt ─────────────────────────────────────────────────────
    // POST /api/admin/instructor-applications/{id}/approve
    @PostMapping("/api/admin/instructor-applications/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        try {
            InstructorApplication app = svc.approve(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Đã duyệt! " + app.getUser().getUsername() + " trở thành Giảng viên.",
                    "status",  app.getStatus().name()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Admin: từ chối ───────────────────────────────────────────────────
    // POST /api/admin/instructor-applications/{id}/reject
    @PostMapping("/api/admin/instructor-applications/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id,
                                    @RequestBody Map<String, String> body) {
        try {
            svc.reject(id, body.getOrDefault("adminNote", ""));
            return ResponseEntity.ok(Map.of("message", "Đã từ chối đơn."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}