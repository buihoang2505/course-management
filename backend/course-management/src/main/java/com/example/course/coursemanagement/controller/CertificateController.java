package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.dto.CertificateDTO;
import com.example.course.coursemanagement.service.CertificateService;
import jakarta.persistence.EntityNotFoundException;
import com.example.course.coursemanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certService;
    private final UserRepository       userRepository;

    // ── Lấy tất cả chứng chỉ của tôi ──────────────────────────
    @GetMapping("/my")
    public ResponseEntity<List<CertificateDTO>> getMyCertificates(
            @RequestParam Long userId) {
        return ResponseEntity.ok(certService.getMyCertificates(userId));
    }

    // ── Lấy chứng chỉ của 1 course cụ thể ──────────────────────
    @GetMapping("/course/{courseId}")
    public ResponseEntity<CertificateDTO> getCertificateForCourse(
            @PathVariable Long courseId,
            @RequestParam Long userId) {
        CertificateDTO dto = certService.getCertificateForCourse(userId, courseId);
        if (dto == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(dto);
    }

    // ── Kiểm tra điều kiện (chi tiết) ──────────────────────────
    @GetMapping("/eligibility")
    public ResponseEntity<Map<String, Object>> checkEligibility(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        return ResponseEntity.ok(certService.checkEligibilityDetail(userId, courseId));
    }

    // ── Cấp chứng chỉ — userId từ JWT ──────────────────────────
    @PostMapping("/claim")
    public ResponseEntity<?> claimCertificate(
            @RequestParam Long courseId,
            @AuthenticationPrincipal UserDetails principal) {
        try {
            Long userId = resolveUserId(principal);
            CertificateDTO dto = certService.tryIssueCertificate(userId, courseId);
            if (dto == null) {
                // Lấy chi tiết để trả về lý do cụ thể
                Map<String, Object> detail = certService.checkEligibilityDetail(userId, courseId);
                boolean allLessonsDone = Boolean.TRUE.equals(detail.get("allLessonsDone"));
                boolean allQuizPassed  = Boolean.TRUE.equals(detail.get("allQuizPassed"));
                String reason = !allLessonsDone
                        ? "Chua hoan thanh het bai hoc (" + detail.get("completedLessons")
                        + "/" + detail.get("totalLessons") + ")"
                        : !allQuizPassed
                        ? "Chua pass het quiz (" + detail.get("passedQuizzes")
                        + "/" + detail.get("totalQuizzes") + ")"
                        : "Diem trung binh quiz chua dat 50%";
                return ResponseEntity.badRequest().body(Map.of("error", reason));
            }
            return ResponseEntity.ok(dto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"))
                .getId();
    }

    // ── Verify chứng chỉ bằng code (PUBLIC) ────────────────────
    @GetMapping("/verify/{code}")
    public ResponseEntity<?> verifyCertificate(@PathVariable String code) {
        try {
            return ResponseEntity.ok(certService.getCertificateByCode(code));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ══════════════════════════════════════════════════════════
    //  ADMIN endpoints
    // ══════════════════════════════════════════════════════════

    /**
     * GET /api/certificates/admin/all
     * Lấy tất cả chứng chỉ, có thể lọc theo courseId
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<CertificateDTO>> getAllCertificates(
            @RequestParam(required = false) Long courseId) {
        return ResponseEntity.ok(certService.getAllCertificates(courseId));
    }

    /**
     * GET /api/certificates/admin/stats
     * Thống kê: tổng chứng chỉ, phân theo khóa học
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        return ResponseEntity.ok(certService.getAdminStats());
    }

    /**
     * DELETE /api/certificates/admin/{id}
     * Thu hồi chứng chỉ
     */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> revokeCertificate(@PathVariable Long id) {
        try {
            certService.revokeCertificate(id);
            return ResponseEntity.ok(Map.of("message", "Đã thu hồi chứng chỉ"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}