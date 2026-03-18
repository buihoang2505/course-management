package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.entity.Course;
import com.example.course.coursemanagement.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor  // Lombok tự tạo constructor inject CourseRepository
@Transactional            // Mọi method đều chạy trong transaction
public class CourseService {

    private final CourseRepository         courseRepository;
    private final ReviewRepository          reviewRepository;
    private final PaymentRepository         paymentRepository;
    private final CertificateRepository     certificateRepository;

    // ====== CREATE ======
    public Course createCourse(Course course) {
        // Tự động set isFree dựa trên price nếu chưa set
        if (course.getIsFree() == null) {
            course.setIsFree(course.getPrice() == null || course.getPrice() == 0);
        }
        return courseRepository.save(course);
    }

    // ====== READ ALL ======
    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // ====== READ ACTIVE ONLY ======
    @Transactional(readOnly = true)
    public List<Course> getActiveCourses() {
        return courseRepository.findByStatus(Course.CourseStatus.ACTIVE);
    }

    // ====== READ ONE ======
    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khóa học với id: " + id));
    }

    // ====== READ WITH LESSONS ======
    @Transactional(readOnly = true)
    public Course getCourseWithLessons(Long id) {
        return courseRepository.findByIdWithLessons(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khóa học với id: " + id));
    }

    // ====== SEARCH ======
    @Transactional(readOnly = true)
    public List<Course> searchCourses(String keyword) {
        return courseRepository.findByTitleContainingIgnoreCase(keyword);
    }

    // ====== UPDATE ======
    public Course updateCourse(Long id, Course updatedCourse) {
        Course existing = getCourseById(id);
        existing.setTitle(updatedCourse.getTitle());
        existing.setDescription(updatedCourse.getDescription());
        existing.setCredits(updatedCourse.getCredits());
        existing.setInstructor(updatedCourse.getInstructor());
        existing.setStatus(updatedCourse.getStatus());
        // FIX: lưu price + isFree khi admin update
        if (updatedCourse.getPrice() != null) {
            existing.setPrice(updatedCourse.getPrice());
            // Tự động sync isFree theo price — tránh inconsistency
            existing.setIsFree(updatedCourse.getPrice() == 0);
        }
        if (updatedCourse.getIsFree() != null) existing.setIsFree(updatedCourse.getIsFree());
        return courseRepository.save(existing);
    }


    // ====== VALIDATE CART IDs ======
    @Transactional(readOnly = true)
    public List<Long> validateCartIds(List<Long> ids) {
        return courseRepository.findAllById(ids).stream()
                .filter(c -> c.getStatus() == Course.CourseStatus.ACTIVE)
                .map(Course::getId)
                .collect(java.util.stream.Collectors.toList());
    }
    // ====== DELETE ======
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        // Xóa các bảng không cascade từ Course entity
        reviewRepository.deleteByCourseId(id);      // reviews
        certificateRepository.deleteByCourseId(id); // certificates
        paymentRepository.deleteByCourseId(id);     // payments
        // Lesson, Enrollment và con cháu cascade ALL → tự xóa
        courseRepository.delete(course);
    }
}