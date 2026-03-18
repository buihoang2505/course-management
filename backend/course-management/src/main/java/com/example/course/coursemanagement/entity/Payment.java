package com.example.course.coursemanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_pay_user",   columnList = "user_id"),
        @Index(name = "idx_pay_course", columnList = "course_id"),
        @Index(name = "idx_pay_txn",    columnList = "txn_ref", unique = true)
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã giao dịch duy nhất gửi lên VNPay
    @Column(name = "txn_ref", nullable = false, unique = true, length = 50)
    private String txnRef;

    // Số tiền (VNĐ)
    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    // Mã trả về từ VNPay
    @Column(name = "vnp_transaction_no", length = 50)
    private String vnpTransactionNo;

    // Response code từ VNPay ("00" = thành công)
    @Column(name = "vnp_response_code", length = 10)
    private String vnpResponseCode;

    // Thông tin bổ sung
    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // ── Relationships ──────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    public enum PaymentStatus {
        PENDING,   // Chờ thanh toán
        SUCCESS,   // Thanh toán thành công
        FAILED,    // Thất bại
        CANCELLED  // Đã hủy
    }
}