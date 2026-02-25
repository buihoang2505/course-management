package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.entity.Course;
import com.example.course.coursemanagement.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor  // Lombok tự tạo constructor inject CourseRepository
@Transactional            // Mọi method đều chạy trong transaction
public class CourseService {

    private final CourseRepository courseRepository;

    // ====== CREATE ======
    public Course createCourse(Course course) {
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
        return courseRepository.save(existing);
    }

    // ====== DELETE ======
    public void deleteCourse(Long id) {
        Course course = getCourseById(id); // Kiểm tra tồn tại trước
        courseRepository.delete(course);
    }
}