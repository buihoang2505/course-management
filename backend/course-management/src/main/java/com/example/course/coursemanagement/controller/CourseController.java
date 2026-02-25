package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.entity.Course;
import com.example.course.coursemanagement.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // GET /api/courses – lấy tất cả khóa học
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // GET /api/courses/active – lấy khóa học đang active
    @GetMapping("/active")
    public ResponseEntity<List<Course>> getActiveCourses() {
        return ResponseEntity.ok(courseService.getActiveCourses());
    }

    // GET /api/courses/{id} – lấy 1 khóa học theo id
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    // GET /api/courses/{id}/lessons – lấy khóa học kèm bài học
    @GetMapping("/{id}/lessons")
    public ResponseEntity<Course> getCourseWithLessons(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseWithLessons(id));
    }

    // GET /api/courses/search?keyword=java – tìm kiếm
    @GetMapping("/search")
    public ResponseEntity<List<Course>> searchCourses(@RequestParam String keyword) {
        return ResponseEntity.ok(courseService.searchCourses(keyword));
    }

    // POST /api/courses – tạo khóa học mới (ADMIN)
    @PostMapping
    public ResponseEntity<Course> createCourse(@Valid @RequestBody Course course) {
        return ResponseEntity.status(201).body(courseService.createCourse(course));
    }

    // PUT /api/courses/{id} – cập nhật khóa học (ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id,
                                               @Valid @RequestBody Course course) {
        return ResponseEntity.ok(courseService.updateCourse(id, course));
    }

    // DELETE /api/courses/{id} – xóa khóa học (ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}