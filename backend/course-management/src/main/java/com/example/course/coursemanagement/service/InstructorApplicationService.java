package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.entity.*;
import com.example.course.coursemanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InstructorApplicationService {

    private final InstructorApplicationRepository appRepo;
    private final UserRepository                  userRepo;
    private final NotificationService             notifService;

    // ── Student nộp đơn ──────────────────────────────────────────────
    public InstructorApplication apply(Long userId, String experience,
                                       String expertise, String portfolioUrl) {
        User user = userRepo.findByIdWithProfile(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại!"));

        // Chỉ STUDENT mới được apply
        if (user.getRole() != Role.STUDENT)
            throw new IllegalStateException("Chỉ tài khoản Student mới có thể nộp đơn Giảng viên!");

        // Không apply khi đang PENDING
        if (appRepo.existsByUserIdAndStatus(userId, InstructorApplication.Status.PENDING))
            throw new IllegalStateException("Bạn đang có đơn chờ duyệt. Vui lòng chờ Admin xem xét!");

        // Anti-spam: bị reject trong 30 ngày gần đây → block
        if (appRepo.hasRecentRejection(userId, LocalDateTime.now().minusDays(30)))
            throw new IllegalStateException("Đơn vừa bị từ chối. Vui lòng chờ 30 ngày trước khi nộp lại!");

        // Yêu cầu profile: fullName + bio phải có
        Profile p = user.getProfile();
        if (p == null || p.getFullName() == null || p.getFullName().isBlank())
            throw new IllegalStateException("Vui lòng cập nhật Họ tên trong hồ sơ trước khi nộp đơn!");
        if (p.getBio() == null || p.getBio().isBlank())
            throw new IllegalStateException("Vui lòng điền Giới thiệu bản thân trong hồ sơ trước khi nộp đơn!");

        InstructorApplication app = InstructorApplication.builder()
                .user(user)
                .experience(experience.trim())
                .expertise(expertise != null ? expertise.trim() : null)
                .portfolioUrl(portfolioUrl != null ? portfolioUrl.trim() : null)
                .build();

        InstructorApplication saved = appRepo.save(app);
        log.info("InstructorApplication submitted: userId={} username={}", userId, user.getUsername());
        return saved;
    }

    // ── Admin: lấy danh sách ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<InstructorApplication> getAll() {
        return appRepo.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<InstructorApplication> getByStatus(String status) {
        return appRepo.findByStatusOrderByCreatedAtDesc(
                InstructorApplication.Status.valueOf(status.toUpperCase()));
    }

    // ── Admin: duyệt ─────────────────────────────────────────────────
    public InstructorApplication approve(Long appId) {
        InstructorApplication app = appRepo.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn!"));
        if (app.getStatus() != InstructorApplication.Status.PENDING)
            throw new IllegalStateException("Đơn này đã được xử lý!");

        // Nâng role
        User user = app.getUser();
        user.setRole(Role.INSTRUCTOR);
        userRepo.save(user);

        app.setStatus(InstructorApplication.Status.APPROVED);
        app.setReviewedAt(LocalDateTime.now());
        appRepo.save(app);

        // Đóng tất cả đơn PENDING khác của cùng user (tránh orphan)
        appRepo.findTopByUserIdOrderByCreatedAtDesc(user.getId()).ifPresent(latest -> {
            if (!latest.getId().equals(appId)
                    && latest.getStatus() == InstructorApplication.Status.PENDING) {
                latest.setStatus(InstructorApplication.Status.APPROVED);
                latest.setReviewedAt(LocalDateTime.now());
                appRepo.save(latest);
            }
        });

        notifService.create(user.getId(), Notification.Type.SYSTEM,
                "🎉 Chúc mừng! Đơn Giảng viên được duyệt",
                "Bạn đã trở thành Giảng viên của EduFlow. Hãy vào trang Giảng viên để tạo khóa học!",
                "/instructor");

        log.info("Application APPROVED: appId={} userId={}", appId, user.getId());
        return app;
    }

    // ── Admin: từ chối ───────────────────────────────────────────────
    public InstructorApplication reject(Long appId, String adminNote) {
        InstructorApplication app = appRepo.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn!"));
        if (app.getStatus() != InstructorApplication.Status.PENDING)
            throw new IllegalStateException("Đơn này đã được xử lý!");

        app.setStatus(InstructorApplication.Status.REJECTED);
        app.setAdminNote(adminNote != null ? adminNote.trim() : "");
        app.setReviewedAt(LocalDateTime.now());
        appRepo.save(app);

        String msg = (adminNote != null && !adminNote.isBlank())
                ? "Đơn Giảng viên chưa được duyệt. Lý do: " + adminNote
                : "Đơn Giảng viên chưa được duyệt. Bạn có thể nộp lại sau 30 ngày.";
        notifService.create(app.getUser().getId(), Notification.Type.SYSTEM,
                "❌ Đơn Giảng viên chưa được duyệt", msg, "/apply-instructor");

        log.info("Application REJECTED: appId={} userId={}", appId, app.getUser().getId());
        return app;
    }

    // ── Student: xem đơn của mình ────────────────────────────────────
    @Transactional(readOnly = true)
    public Map<String, Object> getMyApplication(Long userId) {
        return appRepo.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",           a.getId());
                    m.put("status",       a.getStatus().name());
                    m.put("experience",   a.getExperience());
                    m.put("expertise",    a.getExpertise() != null ? a.getExpertise() : "");
                    m.put("portfolioUrl", a.getPortfolioUrl() != null ? a.getPortfolioUrl() : "");
                    m.put("adminNote",    a.getAdminNote() != null ? a.getAdminNote() : "");
                    m.put("createdAt",    a.getCreatedAt().toString());
                    return m;
                })
                .orElse(null);
    }
}