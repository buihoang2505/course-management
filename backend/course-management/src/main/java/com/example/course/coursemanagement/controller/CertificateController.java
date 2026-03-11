package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.dto.CertificateDTO;
import com.example.course.coursemanagement.service.CertificateService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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

    // ── Kiểm tra điều kiện ─────────────────────────────────────
    @GetMapping("/eligibility")
    public ResponseEntity<Map<String, Boolean>> checkEligibility(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        boolean eligible = certService.checkEligibility(userId, courseId);
        return ResponseEntity.ok(Map.of("eligible", eligible));
    }

    // ── Cấp chứng chỉ ──────────────────────────────────────────
    @PostMapping("/claim")
    public ResponseEntity<?> claimCertificate(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        try {
            CertificateDTO dto = certService.tryIssueCertificate(userId, courseId);
            if (dto == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Bạn chưa đủ điều kiện nhận chứng chỉ. " +
                                "Hãy hoàn thành tất cả bài học và pass tất cả quiz."));
            }
            return ResponseEntity.ok(dto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
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