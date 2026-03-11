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
    // ════════════════════════════════════════════════════

    public CertificateDTO tryIssueCertificate(Long userId, Long courseId) {
        if (certRepo.existsByUserIdAndCourseId(userId, courseId)) {
            return certRepo.findByUserIdAndCourseId(userId, courseId)
                    .map(CertificateDTO::from).orElseThrow();
        }

        List<Lesson> allLessons = lessonRepo.findByCourseIdOrderByOrderNum(courseId);
        if (allLessons.isEmpty()) return null;

        int totalLessons = allLessons.size();
        long completedCount = completionRepo.countByUserIdAndCourseId(userId, courseId);
        if (completedCount < totalLessons) return null;

        boolean allQuizPassed = true;
        double totalScore = 0.0;
        int quizCount = 0;

        for (Lesson lesson : allLessons) {
            var quizOpt = quizRepo.findActiveByLessonId(lesson.getId());
            if (quizOpt.isEmpty()) continue;
            Quiz quiz = quizOpt.get();
            quizCount++;
            if (!attemptRepo.existsByQuizIdAndUserIdAndPassedTrue(quiz.getId(), userId)) {
                allQuizPassed = false;
                break;
            }
            OptionalDouble best = attemptRepo
                    .findByQuizIdAndUserIdOrderByStartedAtDesc(quiz.getId(), userId)
                    .stream().filter(QuizAttempt::getPassed)
                    .mapToDouble(QuizAttempt::getScore).max();
            totalScore += best.orElse(0.0);
        }

        if (!allQuizPassed) return null;

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        double avgScore = quizCount > 0
                ? Math.round(totalScore / quizCount * 10.0) / 10.0
                : 100.0;

        String studentName = user.getProfile() != null
                && user.getProfile().getFullName() != null
                && !user.getProfile().getFullName().isBlank()
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

        // In-app notification
        notificationService.notifyCertificate(userId, course.getTitle(), cert.getCertificateCode());

        // ── Email cho học viên + notify admin ─────────────────
        try {
            emailService.sendCertificate(
                    studentName, user.getEmail(),
                    course.getTitle(), cert.getCertificateCode(),
                    totalLessons, avgScore);
        } catch (Exception e) {
            log.warn("Failed to enqueue certificate email for {}: {}",
                    user.getUsername(), e.getMessage());
        }

        return CertificateDTO.from(cert);
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
                        "Không tìm thấy chứng chỉ với mã: " + code));
    }

    @Transactional(readOnly = true)
    public CertificateDTO getCertificateForCourse(Long userId, Long courseId) {
        return certRepo.findByUserIdAndCourseId(userId, courseId)
                .map(CertificateDTO::from).orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean checkEligibility(Long userId, Long courseId) {
        List<Lesson> allLessons = lessonRepo.findByCourseIdOrderByOrderNum(courseId);
        if (allLessons.isEmpty()) return false;
        long completed = completionRepo.countByUserIdAndCourseId(userId, courseId);
        if (completed < allLessons.size()) return false;
        for (Lesson lesson : allLessons) {
            var quizOpt = quizRepo.findActiveByLessonId(lesson.getId());
            if (quizOpt.isEmpty()) continue;
            if (!attemptRepo.existsByQuizIdAndUserIdAndPassedTrue(quizOpt.get().getId(), userId))
                return false;
        }
        return true;
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
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy chứng chỉ id=" + certId));
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