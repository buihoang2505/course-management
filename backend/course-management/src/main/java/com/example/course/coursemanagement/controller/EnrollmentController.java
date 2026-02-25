package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.dto.EnrollmentDTO;
import com.example.course.coursemanagement.entity.Enrollment;
import com.example.course.coursemanagement.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // POST /api/enrollments/enroll?userId=1&courseId=2
    @PostMapping("/enroll")
    public ResponseEntity<?> enroll(@RequestParam Long userId,
                                    @RequestParam Long courseId) {
        try {
            Enrollment e = enrollmentService.enrollCourse(userId, courseId);
            return ResponseEntity.ok(EnrollmentDTO.from(e));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // DELETE /api/enrollments/unenroll?userId=1&courseId=2
    @DeleteMapping("/unenroll")
    public ResponseEntity<?> unenroll(@RequestParam Long userId,
                                      @RequestParam Long courseId) {
        try {
            enrollmentService.unenrollCourse(userId, courseId);
            return ResponseEntity.ok(Map.of("message", "Hủy đăng ký thành công"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // GET /api/enrollments/user/{userId}  → khóa học của user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EnrollmentDTO>> getUserEnrollments(@PathVariable Long userId) {
        List<EnrollmentDTO> result = enrollmentService.getUserEnrollments(userId)
                .stream()
                .map(EnrollmentDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // GET /api/enrollments/course/{courseId}  → học viên của khóa học (ADMIN)
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<EnrollmentDTO>> getCourseEnrollments(@PathVariable Long courseId) {
        List<EnrollmentDTO> result = enrollmentService.getCourseEnrollments(courseId)
                .stream()
                .map(EnrollmentDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}