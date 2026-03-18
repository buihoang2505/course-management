package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.dto.CertificateDTO;
import com.example.course.coursemanagement.entity.*;
import com.example.course.coursemanagement.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CertificateService {

    private final CertificateRepository      certRepo;
    private final LessonRepository           lessonRepo;
    private final LessonCompletionRepository completionRepo;
    private final QuizRepository             quizRepo;
    private final QuizAttemptRepository      attemptRepo;
    private final UserRepository             userRepo;
    private final CourseRepository           courseRepo;
    private final NotificationService        notificationService;
    private final EmailService               emailService;

    // ════════════════════════════════════════════════════
    //  KIỂM TRA VÀ CẤP CHỨNG CHỈ
    //
    //  Điều kiện (giống Udemy/Coursera):
    //  1. Hoàn thành 100% bài học
    //  2. Nếu khóa học CÓ quiz → phải pass TẤT CẢ quiz (score >= passingScore)
    //  3. Nếu khóa học KHÔNG CÓ quiz → chỉ cần hoàn thành 100% bài học
    //  4. Điểm TB quiz >= 50% (ngưỡng tối thiểu toàn khóa)
    // ════════════════════════════════════════════════════

    public CertificateDTO tryIssueCertificate(Long userId, Long courseId) {
        // Đã có chứng chỉ → trả lại luôn
        if (certRepo.existsByUserIdAndCourseId(userId, courseId)) {
            return certRepo.findByUserIdAndCourseId(userId, courseId)
                    .map(CertificateDTO::from).orElseThrow();
        }

        List<Lesson> allLessons = lessonRepo.findByCourseIdOrderByOrderNum(courseId);

        // Khóa không có bài học → không cấp
        if (allLessons.isEmpty()) return null;

        int totalLessons = allLessons.size();

        // ── Điều kiện 1: phải hoàn thành 100% bài học ──
        long completedCount = completionRepo.countByUserIdAndCourseId(userId, courseId);
        if (completedCount < totalLessons) {
            log.debug("[Cert] userId={} courseId={}: chưa hoàn thành hết bài ({}/{})",
                    userId, courseId, completedCount, totalLessons);
            return null;
        }

        // ── Điều kiện 2: kiểm tra quiz ──
        double totalScore = 0.0;
        int    quizCount  = 0;
        boolean hasAnyQuiz = false;

        for (Lesson lesson : allLessons) {
            var quizOpt = quizRepo.findActiveByLessonId(lesson.getId());
            if (quizOpt.isEmpty()) continue;

            hasAnyQuiz = true;
            Quiz quiz  = quizOpt.get();
            quizCount++;

            // Chưa pass quiz này → không đủ điều kiện
            if (!attemptRepo.existsByQuizIdAndUserIdAndPassedTrue(quiz.getId(), userId)) {
                log.debug("[Cert] userId={} courseId={}: chưa pass quizId={}",
                        userId, courseId, quiz.getId());
                return null;
            }

            // Lấy điểm cao nhất của các lần pass
            OptionalDouble best = attemptRepo
                    .findByQuizIdAndUserIdOrderByStartedAtDesc(quiz.getId(), userId)
                    .stream()
                    .filter(QuizAttempt::getPassed)
                    .mapToDouble(QuizAttempt::getScore)
                    .max();
            totalScore += best.orElse(0.0);
        }

        // ── Tính avgScore ──
        // Nếu có quiz: tính TB thực; nếu không có quiz: avgScore = 0 (hiển thị N/A)
        double avgScore = (hasAnyQuiz && quizCount > 0)
                ? Math.round(totalScore / quizCount * 10.0) / 10.0
                : 0.0;

        // ── Điều kiện 3 (chỉ khi có quiz): điểm TB toàn khóa >= 50 ──
        if (hasAnyQuiz && avgScore < 50.0) {
            log.debug("[Cert] userId={} courseId={}: avgScore={} < 50, không đủ điều kiện",
                    userId, courseId, avgScore);
            return null;
        }

        // ── Đủ điều kiện → cấp chứng chỉ ──
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        String studentName = (user.getProfile() != null
                && user.getProfile().getFullName() != null
                && !user.getProfile().getFullName().isBlank())
                ? user.getProfile().getFullName()
                : user.getUsername();

        Certificate cert = Certificate.builder()
                .user(user).course(course)
                .courseTitle(course.getTitle())
                .studentName(studentName)
                .averageQuizScore(avgScore)
                .totalLessons(totalLessons)
                .build();

        cert = certRepo.save(cert);
        log.info("[Cert] Issued certificate id={} to userId={} for courseId={} avgScore={}",
                cert.getId(), userId, courseId, avgScore);

        notificationService.notifyCertificate(userId, course.getTitle(), cert.getCertificateCode());

        try {
            emailService.sendCertificate(
                    studentName, user.getEmail(),
                    course.getTitle(), cert.getCertificateCode(),
                    totalLessons, avgScore);
        } catch (Exception e) {
            log.warn("[Cert] Failed to send certificate email for userId={}: {}", userId, e.getMessage());
        }

        return CertificateDTO.from(cert);
    }

    // ════════════════════════════════════════════════════
    //  CHECK ELIGIBILITY — dùng cho frontend hiển thị nút Nhận chứng chỉ
    //  Trả về chi tiết để frontend biết còn thiếu gì
    // ════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public Map<String, Object> checkEligibilityDetail(Long userId, Long courseId) {
        List<Lesson> allLessons = lessonRepo.findByCourseIdOrderByOrderNum(courseId);
        if (allLessons.isEmpty()) {
            return Map.of("eligible", false, "reason", "Khoa hoc chua co bai hoc");
        }

        int  totalLessons   = allLessons.size();
        long completedCount = completionRepo.countByUserIdAndCourseId(userId, courseId);
        boolean allLessonsDone = completedCount >= totalLessons;

        int  totalQuizzes    = 0;
        int  passedQuizzes   = 0;
        boolean hasQuiz      = false;

        for (Lesson lesson : allLessons) {
            var quizOpt = quizRepo.findActiveByLessonId(lesson.getId());
            if (quizOpt.isEmpty()) continue;
            hasQuiz = true;
            totalQuizzes++;
            if (attemptRepo.existsByQuizIdAndUserIdAndPassedTrue(quizOpt.get().getId(), userId)) {
                passedQuizzes++;
            }
        }

        boolean allQuizPassed = !hasQuiz || passedQuizzes >= totalQuizzes;
        boolean eligible = allLessonsDone && allQuizPassed;

        return Map.of(
                "eligible",       eligible,
                "totalLessons",   totalLessons,
                "completedLessons", completedCount,
                "allLessonsDone", allLessonsDone,
                "totalQuizzes",   totalQuizzes,
                "passedQuizzes",  passedQuizzes,
                "allQuizPassed",  allQuizPassed,
                "hasQuiz",        hasQuiz
        );
    }

    @Transactional(readOnly = true)
    public boolean checkEligibility(Long userId, Long courseId) {
        Map<String, Object> detail = checkEligibilityDetail(userId, courseId);
        return Boolean.TRUE.equals(detail.get("eligible"));
    }

    // ════════════════════════════════════════════════════
    //  GET — học viên
    // ════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<CertificateDTO> getMyCertificates(Long userId) {
        return certRepo.findByUserIdWithCourse(userId)
                .stream().map(CertificateDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public CertificateDTO getCertificateByCode(String code) {
        return certRepo.findByCertificateCode(code)
                .map(CertificateDTO::from)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Khong tim thay chung chi voi ma: " + code));
    }

    @Transactional(readOnly = true)
    public CertificateDTO getCertificateForCourse(Long userId, Long courseId) {
        return certRepo.findByUserIdAndCourseId(userId, courseId)
                .map(CertificateDTO::from).orElse(null);
    }

    // ════════════════════════════════════════════════════
    //  ADMIN
    // ════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<CertificateDTO> getAllCertificates(Long courseId) {
        if (courseId != null)
            return certRepo.findByCourseIdWithUser(courseId)
                    .stream().map(CertificateDTO::from).toList();
        return certRepo.findAllWithUserAndCourse()
                .stream().map(CertificateDTO::from).toList();
    }

    public void revokeCertificate(Long certId) {
        Certificate cert = certRepo.findById(certId)
                .orElseThrow(() -> new EntityNotFoundException("Khong tim thay chung chi id=" + certId));
        certRepo.delete(cert);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminStats() {
        long total = certRepo.count();
        List<Object[]> byCourse = certRepo.countByCourseTitle();
        List<Map<String, Object>> breakdown = byCourse.stream()
                .map(row -> Map.<String, Object>of("courseTitle", row[0], "count", row[1]))
                .toList();
        return Map.of("total", total, "byCourse", breakdown);
    }
}