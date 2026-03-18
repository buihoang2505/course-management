package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.dto.ProgressDTO;
import com.example.course.coursemanagement.entity.*;
import com.example.course.coursemanagement.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LessonCompletionService {

    private final LessonCompletionRepository completionRepo;
    private final LessonRepository           lessonRepo;
    private final UserRepository             userRepo;
    private final EnrollmentRepository       enrollmentRepo;
    private final NotificationService        notificationService;
    private final CertificateService         certificateService;
    private final EmailService               emailService;   // ← inject

    public ProgressDTO markComplete(Long userId, Long lessonId) {
        if (completionRepo.existsByUserIdAndLessonId(userId, lessonId)) {
            throw new IllegalStateException("Bài học này đã được đánh dấu hoàn thành!");
        }

        Lesson lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài học!"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user!"));

        // Kiểm tra user đã enroll khóa học này chưa
        Long courseId = lesson.getCourse().getId();
        if (!enrollmentRepo.existsByUserIdAndCourseId(userId, courseId)) {
            throw new IllegalStateException("Bạn chưa đăng ký khóa học này!");
        }

        LessonCompletion completion = new LessonCompletion();
        completion.setUser(user);
        completion.setLesson(lesson);
        completionRepo.save(completion);
        return calculateAndAutoComplete(userId, courseId, user);
    }

    public ProgressDTO unmarkComplete(Long userId, Long lessonId) {
        LessonCompletion completion = completionRepo
                .findByUserIdAndLessonId(userId, lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Chưa đánh dấu bài học này!"));

        Long courseId = completion.getLesson().getCourse().getId();
        completionRepo.delete(completion);
        return calculateProgress(userId, courseId);
    }

    @Transactional(readOnly = true)
    public ProgressDTO getProgress(Long userId, Long courseId) {
        return calculateProgress(userId, courseId);
    }

    // ── PRIVATE ───────────────────────────────────────────────

    private ProgressDTO calculateProgress(Long userId, Long courseId) {
        List<Lesson> allLessons = lessonRepo.findByCourseIdOrderByOrderNum(courseId);
        int total = allLessons.size();
        long completed = completionRepo.countByUserIdAndCourseId(userId, courseId);
        List<Long> completedIds = completionRepo
                .findCompletedLessonIdsByUserAndCourse(userId, courseId);
        String courseTitle = allLessons.isEmpty() ? ""
                : allLessons.get(0).getCourse().getTitle();
        return ProgressDTO.of(courseId, courseTitle, total, completed, completedIds);
    }

    private ProgressDTO calculateAndAutoComplete(Long userId, Long courseId, User user) {
        ProgressDTO progress = calculateProgress(userId, courseId);

        if (progress.isCourseCompleted()) {
            enrollmentRepo.findByUserIdWithCourse(userId).stream()
                    .filter(e -> e.getCourse() != null
                            && e.getCourse().getId().equals(courseId)
                            && e.getStatus() != Enrollment.EnrollmentStatus.COMPLETED)
                    .findFirst()
                    .ifPresent(enrollment -> {
                        enrollment.setStatus(Enrollment.EnrollmentStatus.COMPLETED);
                        enrollmentRepo.save(enrollment);

                        // In-app notification
                        notificationService.notifyCompletion(userId, progress.getCourseTitle());

                        // ── Email: học viên hoàn thành khóa học ───────────
                        try {
                            emailService.sendCourseCompleted(
                                    user.getUsername(),
                                    user.getEmail(),
                                    progress.getCourseTitle());
                        } catch (Exception ex) {
                            log.warn("Failed to enqueue course-completed email for {}: {}",
                                    user.getUsername(), ex.getMessage());
                        }
                    });

            // Auto check và cấp chứng chỉ
            try {
                certificateService.tryIssueCertificate(userId, courseId);
            } catch (Exception e) {
                log.warn("[LessonCompletionService] certificate check error: {}", e.getMessage());
            }
        }

        return progress;
    }
}