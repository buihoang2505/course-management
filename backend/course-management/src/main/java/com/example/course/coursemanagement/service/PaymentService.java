package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.entity.*;
import com.example.course.coursemanagement.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import com.example.course.coursemanagement.payment.VNPayUtil; // dùng cho handleCallback (VNPay IPN cũ)
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository    paymentRepo;
    private final CourseRepository     courseRepo;
    private final UserRepository       userRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final NotificationService  notificationService;
    private final EmailService         emailService;

    // hashSecret cần cho handleCallback (xác thực chữ ký VNPay IPN)
    @Value("${vnpay.hash-secret:}")
    private String hashSecret;

    // ════════════════════════════════════════════════════
    //  TẠO LINK THANH TOÁN
    // ════════════════════════════════════════════════════
    /**
     * MOCK PAYMENT — không dùng VNPay.
     * Trả về thông tin để FE hiển thị trang xác nhận nội bộ.
     */
    public Map<String, Object> createPayment(Long userId, Long courseId, String ipAddr) throws Exception {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khóa học!"));

        if (enrollmentRepo.existsByUserIdAndCourseId(userId, courseId))
            throw new IllegalStateException("Bạn đã đăng ký khóa học này rồi!");

        // FIX: dùng price làm nguồn sự thật — nếu price=null hoặc price=0 thì là miễn phí
        // isFree có thể bị sai default trong DB nên không dùng làm điều kiện chính
        boolean courseIsFree = (course.getPrice() == null || course.getPrice() == 0);
        if (courseIsFree)
            throw new IllegalStateException("Khóa học này miễn phí, vui lòng đăng ký trực tiếp!");

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user!"));

        String txnRef = "MOCK-" + System.currentTimeMillis() + "-" + courseId;

        Payment payment = Payment.builder()
                .txnRef(txnRef)
                .amount(course.getPrice())
                .user(user).course(course)
                .description("Thanh toán khóa học: " + course.getTitle())
                .build();
        paymentRepo.save(payment);

        log.info("[MockPayment] Created txnRef={} userId={} courseId={} amount={}",
                txnRef, userId, courseId, course.getPrice());

        return Map.of(
                "txnRef",      txnRef,
                "amount",      course.getPrice(),
                "courseId",    course.getId(),
                "courseTitle", course.getTitle(),
                "mock",        true
        );
    }

    /**
     * Học viên xác nhận thanh toán mock.
     * Cập nhật Payment → SUCCESS, tự động enroll, gửi email.
     */
    public Map<String, Object> confirmMockPayment(String txnRef) {
        Payment payment = paymentRepo.findByTxnRef(txnRef)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giao dịch!"));

        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS)
            return Map.of("success", true, "courseId", payment.getCourse().getId());

        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        payment.setVnpResponseCode("00");
        paymentRepo.save(payment);

        // Tự động enroll
        if (!enrollmentRepo.existsByUserIdAndCourseId(
                payment.getUser().getId(), payment.getCourse().getId())) {
            Enrollment enrollment = Enrollment.builder()
                    .user(payment.getUser()).course(payment.getCourse())
                    .status(Enrollment.EnrollmentStatus.ACTIVE).build();
            enrollmentRepo.save(enrollment);
        }

        notificationService.create(
                payment.getUser().getId(), Notification.Type.SYSTEM,
                "Thanh toán thành công!",
                "Bạn đã đăng ký: " + payment.getCourse().getTitle(),
                "/courses/" + payment.getCourse().getId());

        try {
            emailService.sendEnrollment(
                    payment.getUser().getUsername(), payment.getUser().getEmail(),
                    payment.getCourse().getTitle(),
                    payment.getCourse().getInstructor() != null
                            ? payment.getCourse().getInstructor() : "EduFlow Academy");
        } catch (Exception e) {
            log.warn("[MockPayment] Email failed: {}", e.getMessage());
        }

        log.info("[MockPayment] CONFIRMED txnRef={} courseId={}", txnRef, payment.getCourse().getId());
        return Map.of("success", true, "courseId", payment.getCourse().getId());
    }

    // ════════════════════════════════════════════════════
    //  XỬ LÝ KẾT QUẢ TỪ VNPAY (IPN + Return URL)
    //  NOTE: Method này giữ lại để tương thích ngược với vnpay-return endpoint.
    //  Luồng chính hiện tại dùng confirmMockPayment() — không qua VNPay.
    // ════════════════════════════════════════════════════
    public Map<String, Object> handleCallback(Map<String, String> params) throws Exception {
        // Xác thực chữ ký
        if (!VNPayUtil.verifyIpn(params, hashSecret)) {
            log.warn("[Payment] Invalid signature: {}", params);
            return Map.of("success", false, "message", "Invalid signature");
        }

        String txnRef      = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.getOrDefault("vnp_TransactionNo", "");

        Payment payment = paymentRepo.findByTxnRef(txnRef)
                .orElseThrow(() -> new EntityNotFoundException("Khong tim thay giao dich: " + txnRef));

        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            return Map.of("success", true, "message", "Da xu ly truoc do");
        }

        payment.setVnpResponseCode(responseCode);
        payment.setVnpTransactionNo(transactionNo);

        if ("00".equals(responseCode)) {
            // ── Thanh toán thành công ──
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepo.save(payment);

            // Tự động enroll
            if (!enrollmentRepo.existsByUserIdAndCourseId(
                    payment.getUser().getId(), payment.getCourse().getId())) {
                Enrollment enrollment = Enrollment.builder()
                        .user(payment.getUser())
                        .course(payment.getCourse())
                        .status(Enrollment.EnrollmentStatus.ACTIVE)
                        .build();
                enrollmentRepo.save(enrollment);
            }

            // Thông báo
            notificationService.create(
                    payment.getUser().getId(),
                    Notification.Type.SYSTEM,
                    "Thanh toan thanh cong!",
                    "Ban da dang ky khoa hoc: " + payment.getCourse().getTitle(),
                    "/courses/" + payment.getCourse().getId()
            );

            // Email xác nhận
            try {
                emailService.sendEnrollment(
                        payment.getUser().getUsername(),
                        payment.getUser().getEmail(),
                        payment.getCourse().getTitle(),
                        payment.getCourse().getInstructor() != null
                                ? payment.getCourse().getInstructor() : "EduFlow Academy"
                );
            } catch (Exception e) {
                log.warn("[Payment] Failed to send email for txnRef={}: {}", txnRef, e.getMessage());
            }

            log.info("[Payment] SUCCESS txnRef={} userId={} courseId={}",
                    txnRef, payment.getUser().getId(), payment.getCourse().getId());

            return Map.of("success", true, "message", "Thanh toan thanh cong",
                    "courseId", payment.getCourse().getId());
        } else {
            // ── Thất bại ──
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepo.save(payment);
            log.info("[Payment] FAILED txnRef={} code={}", txnRef, responseCode);
            return Map.of("success", false, "message", "Thanh toan that bai, ma loi: " + responseCode);
        }
    }

    // ════════════════════════════════════════════════════
    //  QUERY
    // ════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMyPayments(Long userId) {
        return paymentRepo.findByUserIdWithDetails(userId)
                .stream().map(this::toMap).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllPayments() {
        return paymentRepo.findAllWithDetails()
                .stream().map(this::toMap).toList();
    }

    private Map<String, Object> toMap(Payment p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",              p.getId());
        m.put("txnRef",          p.getTxnRef());
        m.put("amount",          p.getAmount());
        m.put("status",          p.getStatus().name());
        m.put("vnpResponseCode", p.getVnpResponseCode());
        m.put("description",     p.getDescription());
        m.put("createdAt",       p.getCreatedAt() != null ? p.getCreatedAt().toString() : null);
        m.put("paidAt",          p.getPaidAt()    != null ? p.getPaidAt().toString()    : null);
        m.put("courseId",        p.getCourse().getId());
        m.put("courseTitle",     p.getCourse().getTitle());
        m.put("userId",          p.getUser().getId());
        m.put("username",        p.getUser().getUsername());
        return m;
    }
}