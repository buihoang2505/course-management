package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.entity.Course;
import com.example.course.coursemanagement.entity.Enrollment;
import com.example.course.coursemanagement.entity.Role;
import com.example.course.coursemanagement.entity.User;
import com.example.course.coursemanagement.repository.CourseRepository;
import com.example.course.coursemanagement.repository.EnrollmentRepository;
import com.example.course.coursemanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository     courseRepository;
    private final UserRepository       userRepository;
    private final EmailService              emailService;
    private final com.example.course.coursemanagement.repository.PaymentRepository paymentRepository;
    private final com.example.course.coursemanagement.repository.LessonCompletionRepository completionRepository;
    private final com.example.course.coursemanagement.repository.GradeRepository            gradeRepository;
    private final com.example.course.coursemanagement.repository.QuizAttemptRepository      attemptRepository;
    private final com.example.course.coursemanagement.repository.AttemptAnswerRepository   answerRepository;
    private final com.example.course.coursemanagement.repository.CertificateRepository      certificateRepository;

    public Enrollment enrollCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user!"));

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Admin không thể đăng ký khóa học!");
        }

        if (user.getRole() == Role.INSTRUCTOR) {
            throw new IllegalArgumentException("Giảng viên không thể đăng ký khóa học với tư cách học viên!");
        }

        if (user.getRole() == Role.BANNED) {
            throw new IllegalArgumentException("Tài khoản của bạn đã bị khóa, không thể đăng ký khóa học!");
        }

        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new IllegalArgumentException("Bạn đã đăng ký khóa học này rồi!");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khóa học!"));

        if (course.getStatus() != Course.CourseStatus.ACTIVE) {
            throw new IllegalArgumentException("Khoa hoc nay khong mo dang ky!");
        }

        // Kiểm tra nếu khóa học có phí → phải có payment SUCCESS
        // Dùng price làm nguồn sự thật, không dùng isFree (có thể bị sai default)
        boolean isFreeByPrice = (course.getPrice() == null || course.getPrice() == 0);
        if (!isFreeByPrice) {
            boolean hasPaid = paymentRepository.existsByUserIdAndCourseIdAndStatus(
                    userId, courseId,
                    com.example.course.coursemanagement.entity.Payment.PaymentStatus.SUCCESS);
            if (!hasPaid) {
                throw new IllegalStateException("PAYMENT_REQUIRED:" + course.getPrice());
            }
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
        enrollment.setEnrolledAt(LocalDateTime.now());
        Enrollment saved = enrollmentRepository.save(enrollment);

        // ── Enqueue enrollment email ───────────────────────────
        try {
            String instructor = course.getInstructor() != null
                    ? course.getInstructor() : "EduFlow Academy";
            emailService.sendEnrollment(
                    user.getUsername(), user.getEmail(),
                    course.getTitle(), instructor);
        } catch (Exception e) {
            log.warn("Failed to enqueue enrollment email for user {}: {}",
                    user.getUsername(), e.getMessage());
        }

        return saved;
    }

    public void unenrollCourse(Long userId, Long courseId) {
        Enrollment e = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new EntityNotFoundException("Kh\u00f4ng t\u00ecm th\u1ea5y \u0111\u0103ng k\u00fd!"));

        // FIX: JPQL bulk-DELETE kh\u00f4ng trigger JPA CascadeType.ALL
        // Ph\u1ea3i x\u00f3a attempt_answers th\u1ee7 c\u00f4ng tr\u01b0\u1edbc r\u1ed3i m\u1edbi x\u00f3a quiz_attempts.

        // B\u01b0\u1edbc 1: l\u1ea5y IDs attempts thu\u1ed9c course n\u00e0y
        List<Long> attemptIds = attemptRepository
                .findAttemptIdsByUserAndCourse(userId, courseId);

        // B\u01b0\u1edbc 2: x\u00f3a attempt_answers (FK → quiz_attempts)
        if (!attemptIds.isEmpty()) {
            answerRepository.deleteByAttemptIds(attemptIds);
        }

        // B\u01b0\u1edbc 3: x\u00f3a quiz_attempts theo ID (kh\u00f4ng dùng JPQL bulk)
        if (!attemptIds.isEmpty()) {
            attemptRepository.deleteAllById(attemptIds);
        }

        // B\u01b0\u1edbc 4: lesson completions ch\u1ec9 c\u1ee7a course n\u00e0y
        completionRepository.deleteByUserIdAndCourseId(userId, courseId);

        // B\u01b0\u1edbc 5: ch\u1ee9ng ch\u1ec9 ch\u1ec9 c\u1ee7a course n\u00e0y
        certificateRepository.deleteByUserIdAndCourseId(userId, courseId);

        // B\u01b0\u1edbc 6: x\u00f3a enrollment (Grade cascade theo)
        enrollmentRepository.delete(e);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getUserEnrollments(Long userId) {
        return enrollmentRepository.findByUserIdWithCourse(userId);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getCourseEnrollments(Long courseId) {
        return enrollmentRepository.findByCourseIdWithUser(courseId);
    }
}