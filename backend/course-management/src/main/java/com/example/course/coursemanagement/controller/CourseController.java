package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.entity.Course;
import com.example.course.coursemanagement.repository.EnrollmentRepository;
import com.example.course.coursemanagement.repository.ReviewRepository;
import com.example.course.coursemanagement.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService        courseService;
    private final ReviewRepository     reviewRepo;
    private final EnrollmentRepository enrollRepo;

    // ── FIX N+1: build lookup maps trước, rồi map từng course O(1) ────
    private Map<String, Object> toDto(Course course,
                                      Map<Long, long[]> ratingMap,
                                      Map<Long, Long>   enrollMap) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",          course.getId());
        m.put("title",       course.getTitle());
        m.put("description", course.getDescription());
        m.put("credits",     course.getCredits());
        m.put("instructor",  course.getInstructor());
        m.put("status",      course.getStatus() != null ? course.getStatus().name() : null);
        m.put("price",       course.getPrice());
        m.put("isFree",      course.getIsFree());
        m.put("createdAt",   course.getCreatedAt() != null ? course.getCreatedAt().toString() : null);

        // Rating từ lookup map — O(1), không query DB
        long[] ra = ratingMap.getOrDefault(course.getId(), new long[]{0, 0});
        long rc   = ra[0];
        double avg = rc > 0 ? (double) ra[1] / 10.0 : 0.0; // avg*10 đã lưu
        m.put("reviewCount", rc);
        m.put("avgRating",   rc > 0 ? Math.round(avg * 10.0) / 10.0 : null);

        // EnrollCount từ lookup map — O(1)
        m.put("enrollCount", enrollMap.getOrDefault(course.getId(), 0L));
        return m;
    }

    /** Xây dựng lookup maps bằng 2 aggregate queries (thay vì N*2) */
    private Map<Long, long[]> buildRatingMap() {
        Map<Long, long[]> map = new HashMap<>();
        for (Object[] row : reviewRepo.countAndAvgGroupedByCourse()) {
            Long   cid   = ((Number) row[0]).longValue();
            long   count = ((Number) row[1]).longValue();
            double avg   = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            // Lưu avg*10 dưới dạng long để tránh floating point
            map.put(cid, new long[]{count, Math.round(avg * 10)});
        }
        return map;
    }

    private Map<Long, Long> buildEnrollMap() {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : enrollRepo.countGroupedByCourse()) {
            map.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return map;
    }

    private List<Map<String, Object>> toDtoList(List<Course> courses) {
        Map<Long, long[]> ratingMap = buildRatingMap();   // 1 query
        Map<Long, Long>   enrollMap  = buildEnrollMap();  // 1 query
        return courses.stream()
                .map(c -> toDto(c, ratingMap, enrollMap))
                .collect(Collectors.toList());
    }

    // GET /api/courses
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllCourses() {
        return ResponseEntity.ok(toDtoList(courseService.getAllCourses()));
    }

    // GET /api/courses/active
    @GetMapping("/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveCourses() {
        return ResponseEntity.ok(toDtoList(courseService.getActiveCourses()));
    }

    // GET /api/courses/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    // GET /api/courses/{id}/lessons
    @GetMapping("/{id}/lessons")
    public ResponseEntity<Course> getCourseWithLessons(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseWithLessons(id));
    }

    // GET /api/courses/search?keyword=java
    @GetMapping("/search")
    public ResponseEntity<List<Course>> searchCourses(@RequestParam String keyword) {
        return ResponseEntity.ok(courseService.searchCourses(keyword));
    }

    // POST /api/courses (ADMIN)
    @PostMapping
    public ResponseEntity<Course> createCourse(@Valid @RequestBody Course course) {
        return ResponseEntity.status(201).body(courseService.createCourse(course));
    }

    // PUT /api/courses/{id} (ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id,
                                               @Valid @RequestBody Course course) {
        return ResponseEntity.ok(courseService.updateCourse(id, course));
    }

    // DELETE /api/courses/{id} (ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/courses/validate-cart
    // Nhận list IDs từ localStorage, trả về những id còn ACTIVE
    @PostMapping("/validate-cart")
    public ResponseEntity<List<Long>> validateCart(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(courseService.validateCartIds(ids));
    }
}