package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.entity.Enrollment;
import com.example.course.coursemanagement.entity.Grade;
import com.example.course.coursemanagement.repository.EnrollmentRepository;
import com.example.course.coursemanagement.repository.GradeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GradeService {

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;

    // ====== NHẬP ĐIỂM ======
    public Grade assignGrade(Long enrollmentId, Double score, String feedback) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy enrollment: " + enrollmentId));

        // Nếu đã có điểm thì cập nhật, chưa có thì tạo mới
        Grade grade = gradeRepository.findByEnrollmentId(enrollmentId)
                .orElse(Grade.builder().enrollment(enrollment).build());

        grade.setScore(score);
        grade.setFeedback(feedback);
        return gradeRepository.save(grade);
    }

    // ====== LẤY ĐIỂM THEO ENROLLMENT ======
    @Transactional(readOnly = true)
    public Grade getGradeByEnrollment(Long enrollmentId) {
        return gradeRepository.findByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Chưa có điểm cho enrollment này!"));
    }

    // ====== LẤY TẤT CẢ ĐIỂM CỦA USER ======
    @Transactional(readOnly = true)
    public List<Grade> getUserGrades(Long userId) {
        return gradeRepository.findAllByUserId(userId);
    }

    // ====== ĐIỂM TRUNG BÌNH KHÓA HỌC ======
    @Transactional(readOnly = true)
    public Double getCourseAverageScore(Long courseId) {
        return gradeRepository.findAverageScoreByCourseId(courseId);
    }
}