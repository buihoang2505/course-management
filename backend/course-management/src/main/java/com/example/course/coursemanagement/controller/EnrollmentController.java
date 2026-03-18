package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.dto.EnrollmentDTO;
import com.example.course.coursemanagement.entity.Enrollment;
import com.example.course.coursemanagement.repository.UserRepository;
import com.example.course.coursemanagement.service.EnrollmentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserRepository    userRepository;

    // POST /api/enrollments/enroll?courseId=2  — userId lấy từ JWT
    @PostMapping("/enroll")
    public ResponseEntity<?> enroll(@RequestParam Long courseId,
                                    @AuthenticationPrincipal UserDetails principal) {
        try {
            Long userId = resolveUserId(principal);
            Enrollment e = enrollmentService.enrollCourse(userId, courseId);
            return ResponseEntity.ok(EnrollmentDTO.from(e));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // DELETE /api/enrollments/unenroll?courseId=2  — userId lấy từ JWT
    @DeleteMapping("/unenroll")
    public ResponseEntity<?> unenroll(@RequestParam Long courseId,
                                      @AuthenticationPrincipal UserDetails principal) {
        try {
            Long userId = resolveUserId(principal);
            enrollmentService.unenrollCourse(userId, courseId);
            return ResponseEntity.ok(Map.of("message", "Huy dang ky thanh cong"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // GET /api/enrollments/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EnrollmentDTO>> getUserEnrollments(@PathVariable Long userId) {
        List<EnrollmentDTO> result = enrollmentService.getUserEnrollments(userId)
                .stream().map(EnrollmentDTO::from).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // GET /api/enrollments/course/{courseId}
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<EnrollmentDTO>> getCourseEnrollments(@PathVariable Long courseId) {
        List<EnrollmentDTO> result = enrollmentService.getCourseEnrollments(courseId)
                .stream().map(EnrollmentDTO::from).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"))
                .getId();
    }
}