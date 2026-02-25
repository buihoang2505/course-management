package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.entity.Course;
import com.example.course.coursemanagement.entity.Enrollment;
import com.example.course.coursemanagement.entity.User;
import com.example.course.coursemanagement.repository.CourseRepository;
import com.example.course.coursemanagement.repository.EnrollmentRepository;
import com.example.course.coursemanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public Enrollment enrollCourse(Long userId, Long courseId) {
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new IllegalArgumentException("Bạn đã đăng ký khóa học này rồi!");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user!"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khóa học!"));

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
        enrollment.setEnrolledAt(LocalDateTime.now());
        return enrollmentRepository.save(enrollment);
    }

    public void unenrollCourse(Long userId, Long courseId) {
        List<Enrollment> list = enrollmentRepository.findByUserIdWithCourse(userId);
        Enrollment e = list.stream()
                .filter(en -> en.getCourse() != null && en.getCourse().getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đăng ký!"));
        enrollmentRepository.delete(e);
    }

    // Lấy khóa học của user - JOIN FETCH course
    @Transactional(readOnly = true)
    public List<Enrollment> getUserEnrollments(Long userId) {
        return enrollmentRepository.findByUserIdWithCourse(userId);
    }

    // Lấy học viên của khóa - JOIN FETCH user + grade
    @Transactional(readOnly = true)
    public List<Enrollment> getCourseEnrollments(Long courseId) {
        return enrollmentRepository.findByCourseIdWithUserAndGrade(courseId);
    }
}