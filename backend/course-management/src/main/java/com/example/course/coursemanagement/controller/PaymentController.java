package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.repository.UserRepository;
import com.example.course.coursemanagement.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService  paymentService;
    private final UserRepository  userRepository;

    // POST /api/payment/create?courseId=X
    // Tạo link thanh toán VNPay
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(
            @RequestParam Long courseId,
            @AuthenticationPrincipal UserDetails principal,
            HttpServletRequest request) {
        try {
            Long userId = resolveUserId(principal);
            String ipAddr = getClientIp(request);
            Map<String, Object> result = paymentService.createPayment(userId, courseId, ipAddr);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/payment/vnpay-return  — VNPay redirect về sau khi thanh toán
    @GetMapping("/vnpay-return")
    public ResponseEntity<?> vnpayReturn(@RequestParam Map<String, String> params) {
        try {
            Map<String, Object> result = paymentService.handleCallback(params);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/payment/vnpay-ipn  — VNPay IPN (server-to-server)
    @PostMapping("/vnpay-ipn")
    public ResponseEntity<?> vnpayIpn(@RequestParam Map<String, String> params) {
        try {
            Map<String, Object> result = paymentService.handleCallback(params);
            if (Boolean.TRUE.equals(result.get("success"))) {
                return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
            }
            return ResponseEntity.ok(Map.of("RspCode", "99", "Message", "Unknown error"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("RspCode", "99", "Message", e.getMessage()));
        }
    }

    // POST /api/payment/confirm?txnRef=MOCK-xxx  — Xác nhận mock payment
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmMock(
            @RequestParam String txnRef,
            @AuthenticationPrincipal UserDetails principal) {
        try {
            Map<String, Object> result = paymentService.confirmMockPayment(txnRef);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/payment/my  — Lịch sử thanh toán của user
    @GetMapping("/my")
    public ResponseEntity<?> myPayments(@AuthenticationPrincipal UserDetails principal) {
        try {
            Long userId = resolveUserId(principal);
            return ResponseEntity.ok(paymentService.getMyPayments(userId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/payment/admin/all  — Admin xem tất cả giao dịch
    @GetMapping("/admin/all")
    public ResponseEntity<?> adminAll() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // ── Helpers ──────────────────────────────────────────
    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"))
                .getId();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip != null ? ip : "127.0.0.1";
    }
}