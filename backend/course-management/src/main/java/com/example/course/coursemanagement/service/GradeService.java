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
    private final LessonRepository           lessonRepository;
    private final LessonCompletionRepository completionRepository;
    private final NotificationService        notificationService;

    // ==================================================
    // 1️⃣ TÍNH ĐIỂM TỰ ĐỘNG dựa trên tiến độ hoàn thành
    //    Công thức: điểm = (bài đã hoàn thành / tổng bài) × 10
    //    VD: 8/10 bài → 8.0 điểm
    // ==================================================
    public Grade calculateAutoGrade(Long enrollmentId) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy enrollment: " + enrollmentId));

        Long userId   = enrollment.getUser().getId();
        Long courseId = enrollment.getCourse().getId();

        // Đếm tổng bài học
        int totalLessons = lessonRepository.countByCourseId(courseId);
        if (totalLessons == 0) {
            throw new IllegalStateException("Khóa học chưa có bài học nào để tính điểm!");
        }

        // Đếm bài đã hoàn thành
        long completedLessons = completionRepository.countByUserIdAndCourseId(userId, courseId);

        // Tính điểm: làm tròn 1 chữ số thập phân
        double progressPercent = (double) completedLessons / totalLessons;
        double score = Math.round(progressPercent * 10.0 * 10) / 10.0; // làm tròn 0.1

        String feedback = String.format(
                "Tính tự động: hoàn thành %d/%d bài học (%.0f%%)",
                completedLessons, totalLessons, progressPercent * 100
        );

        // Upsert grade
        Grade grade = gradeRepository.findByEnrollmentId(enrollmentId)
                .orElseGet(() -> {
                    Grade g = new Grade();
                    g.setEnrollment(enrollment);
                    return g;
                });

        grade.setScore(score);
        grade.setFeedback(feedback);
        Grade saved = gradeRepository.save(grade);

        // Gửi thông báo cho học viên
        String courseName = enrollment.getCourse() != null ? enrollment.getCourse().getTitle() : "khóa học";
        notificationService.notifyGrade(userId, courseName, score);

        return saved;
    }

    // ==================================================
    // 2️⃣ TÍNH ĐIỂM THỦ CÔNG (Admin override nếu muốn)
    //    Giữ lại cho trường hợp admin muốn chỉnh điểm thủ công
    // ==================================================
    public Grade assignGradeManual(Long enrollmentId, Double score, String feedback) {

        if (score < 0 || score > 10)
            throw new IllegalArgumentException("Điểm phải trong khoảng 0–10!");

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy enrollment"));

        Grade grade = gradeRepository.findByEnrollmentId(enrollmentId)
                .orElseGet(() -> {
                    Grade g = new Grade();
                    g.setEnrollment(enrollment);
                    return g;
                });

        grade.setScore(score);
        grade.setFeedback(feedback != null ? feedback : "Nhập thủ công");
        Grade saved = gradeRepository.save(grade);

        String courseName = enrollment.getCourse() != null ? enrollment.getCourse().getTitle() : "khóa học";
        notificationService.notifyGrade(enrollment.getUser().getId(), courseName, score);

        return saved;
    }

    // ==================================================
    // 3️⃣ LẤY ĐIỂM THEO ENROLLMENT
    // ==================================================
    @Transactional(readOnly = true)
    public Grade getGradeByEnrollment(Long enrollmentId) {
        return gradeRepository.findByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Chưa có điểm"));
    }

    // ==================================================
    // 4️⃣ LẤY TẤT CẢ ĐIỂM CỦA 1 USER
    // ==================================================
    @Transactional(readOnly = true)
    public List<Grade> getUserGrades(Long userId) {
        return gradeRepository.findByEnrollment_User_Id(userId);
    }

    // ==================================================
    // 5️⃣ ĐIỂM TRUNG BÌNH 1 KHÓA
    // ==================================================
    @Transactional(readOnly = true)
    public Double getCourseAverageScore(Long courseId) {
        return gradeRepository.findAverageScoreByCourseId(courseId);
    }
}