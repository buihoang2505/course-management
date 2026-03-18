package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.entity.*;
import com.example.course.coursemanagement.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GradeService {

    private final EnrollmentRepository       enrollmentRepository;
    private final GradeRepository            gradeRepository;
    private final QuizRepository             quizRepository;
    private final QuizAttemptRepository      attemptRepository;
    private final NotificationService        notificationService;

    // ════════════════════════════════════════════════════
    //  TÍNH ĐIỂM TỰ ĐỘNG — TB điểm cao nhất của các quiz
    //
    //  Công thức (giống updateFinalGrade trong QuizService):
    //    bestScores = điểm cao nhất mỗi quiz user đã làm
    //    finalScore = AVG(bestScores) / 10
    //
    //  Dùng cho Admin khi muốn tính/cập nhật lại điểm thủ công.
    // ════════════════════════════════════════════════════
    public Grade calculateAutoGrade(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Khong tim thay enrollment: " + enrollmentId));

        Long userId   = enrollment.getUser().getId();
        Long courseId = enrollment.getCourse().getId();

        long totalQuizzes = quizRepository.countActiveByCourseId(courseId);
        if (totalQuizzes == 0) {
            throw new IllegalStateException(
                    "Khoa hoc nay chua co quiz nao. Diem se tu dong tinh khi hoc vien lam quiz.");
        }

        List<Double> bestScores = attemptRepository
                .findBestScoresPerQuizByCourse(userId, courseId);

        if (bestScores.isEmpty()) {
            throw new IllegalStateException(
                    "Hoc vien chua lam quiz nao trong khoa hoc nay.");
        }

        double avgScore100 = bestScores.stream()
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
        double score = Math.round(avgScore100 / 10.0 * 10) / 10.0;

        String feedback = String.format(
                "Admin tinh lai: TB diem quiz %.1f/10 (%d/%d quiz da lam)",
                score, bestScores.size(), totalQuizzes);

        Grade grade = gradeRepository.findByEnrollmentId(enrollmentId)
                .orElseGet(() -> {
                    Grade g = new Grade();
                    g.setEnrollment(enrollment);
                    return g;
                });

        // Admin tính lại → ghi đè luôn (không check điểm cũ)
        grade.setScore(score);
        grade.setFeedback(feedback);
        Grade saved = gradeRepository.save(grade);

        // Đồng bộ lên Enrollment
        enrollment.setFinalScore(score);
        enrollment.setResult(score >= 5.0
                ? Enrollment.ResultStatus.PASSED
                : Enrollment.ResultStatus.FAILED);
        enrollmentRepository.save(enrollment);

        // Thông báo cho học viên
        String courseName = enrollment.getCourse() != null
                ? enrollment.getCourse().getTitle() : "khoa hoc";
        notificationService.notifyGrade(userId, courseName, score);

        return saved;
    }

    // ════════════════════════════════════════════════════
    //  NHẬP ĐIỂM THỦ CÔNG (Admin override)
    // ════════════════════════════════════════════════════
    public Grade assignGradeManual(Long enrollmentId, Double score, String feedback) {
        if (score < 0 || score > 10)
            throw new IllegalArgumentException("Diem phai trong khoang 0-10!");

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Khong tim thay enrollment"));

        Grade grade = gradeRepository.findByEnrollmentId(enrollmentId)
                .orElseGet(() -> {
                    Grade g = new Grade();
                    g.setEnrollment(enrollment);
                    return g;
                });

        grade.setScore(score);
        grade.setFeedback(feedback != null ? feedback : "Nhap thu cong");
        Grade saved = gradeRepository.save(grade);

        enrollment.setFinalScore(score);
        enrollment.setResult(score >= 5.0
                ? Enrollment.ResultStatus.PASSED
                : Enrollment.ResultStatus.FAILED);
        enrollmentRepository.save(enrollment);

        String courseName = enrollment.getCourse() != null
                ? enrollment.getCourse().getTitle() : "khoa hoc";
        notificationService.notifyGrade(enrollment.getUser().getId(), courseName, score);

        return saved;
    }

    @Transactional(readOnly = true)
    public Grade getGradeByEnrollment(Long enrollmentId) {
        return gradeRepository.findByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Chua co diem"));
    }

    @Transactional(readOnly = true)
    public List<Grade> getUserGrades(Long userId) {
        return gradeRepository.findByEnrollment_User_Id(userId);
    }

    @Transactional(readOnly = true)
    public Double getCourseAverageScore(Long courseId) {
        return gradeRepository.findAverageScoreByCourseId(courseId);
    }
}