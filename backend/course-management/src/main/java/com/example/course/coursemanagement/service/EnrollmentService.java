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
    private final EmailService         emailService;   // ← inject

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
            throw new IllegalArgumentException("Khóa học này không mở đăng ký!");
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
        List<Enrollment> list = enrollmentRepository.findByUserIdWithCourse(userId);
        Enrollment e = list.stream()
                .filter(en -> en.getCourse() != null && en.getCourse().getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đăng ký!"));
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