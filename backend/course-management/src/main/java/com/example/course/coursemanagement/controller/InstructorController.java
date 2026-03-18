package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.entity.Course;
import com.example.course.coursemanagement.entity.Role;
import com.example.course.coursemanagement.entity.User;
import com.example.course.coursemanagement.repository.CourseRepository;
import com.example.course.coursemanagement.repository.EnrollmentRepository;
import com.example.course.coursemanagement.repository.LessonRepository;
import com.example.course.coursemanagement.entity.Lesson;
import com.example.course.coursemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/instructor")
@RequiredArgsConstructor
public class InstructorController {

    private final CourseRepository     courseRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final LessonRepository     lessonRepo;
    private final UserRepository       userRepo;

    // ── Helper: lấy user từ JWT, kiểm tra quyền INSTRUCTOR ──
    private User getInstructor(Authentication auth) {
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != Role.INSTRUCTOR && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Bạn không có quyền truy cập!");
        }
        return user;
    }

    // ── GET /api/instructor/dashboard ──
    // Stats: totalCourses, activeCourses, totalStudents, totalLessons
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(Authentication auth) {
        User instructor = getInstructor(auth);
        String username = instructor.getUsername();

        List<Course> myCourses = courseRepo.findByInstructor(username);
        long totalCourses  = myCourses.size();
        long activeCourses = myCourses.stream()
                .filter(c -> c.getStatus() == Course.CourseStatus.ACTIVE).count();

        long totalStudents = myCourses.stream()
                .mapToLong(c -> enrollmentRepo.countByCourseId(c.getId()))
                .sum();

        long totalLessons = myCourses.stream()
                .mapToLong(c -> lessonRepo.countByCourseId(c.getId()))
                .sum();

        return ResponseEntity.ok(Map.of(
                "totalCourses",  totalCourses,
                "activeCourses", activeCourses,
                "totalStudents", totalStudents,
                "totalLessons",  totalLessons
        ));
    }

    // ── GET /api/instructor/courses ──
    // Danh sách khóa học của instructor hiện tại
    @GetMapping("/courses")
    public ResponseEntity<?> getCourses(Authentication auth) {
        User instructor = getInstructor(auth);
        List<Map<String, Object>> result = courseRepo.findByInstructor(instructor.getUsername())
                .stream()
                .map(c -> Map.<String, Object>of(
                        "id",          c.getId(),
                        "title",       c.getTitle(),
                        "description", c.getDescription() != null ? c.getDescription() : "",
                        "instructor",  c.getInstructor() != null ? c.getInstructor() : "",
                        "credits",     c.getCredits() != null ? c.getCredits() : 0,
                        "status",      c.getStatus().name(),
                        "createdAt",   c.getCreatedAt() != null ? c.getCreatedAt().toString() : ""
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ── POST /api/instructor/courses ──
    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody Map<String, Object> body,
                                          Authentication auth) {
        User instructor = getInstructor(auth);

        Course course = new Course();
        course.setTitle((String) body.get("title"));
        course.setDescription((String) body.getOrDefault("description", ""));
        // Luôn dùng username của instructor đang đăng nhập
        course.setInstructor(instructor.getUsername());
        Object credits = body.get("credits");
        course.setCredits(credits != null ? Integer.valueOf(credits.toString()) : 3);
        String status = (String) body.getOrDefault("status", "DRAFT");
        course.setStatus(Course.CourseStatus.valueOf(status));
        // Payment fields
        Object price  = body.get("price");
        Object isFree = body.get("isFree");
        course.setPrice(price  != null ? Long.valueOf(price.toString()) : 0L);
        course.setIsFree(isFree != null ? Boolean.parseBoolean(isFree.toString()) : true);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        Course saved = courseRepo.save(course);
        return ResponseEntity.ok(Map.of(
                "id",      saved.getId(),
                "title",   saved.getTitle(),
                "status",  saved.getStatus().name()
        ));
    }

    // ── PUT /api/instructor/courses/{id} ──
    @PutMapping("/courses/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id,
                                          @RequestBody Map<String, Object> body,
                                          Authentication auth) {
        User instructor = getInstructor(auth);

        Course course = courseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học!"));

        // Chỉ cho phép sửa khóa học của chính mình
        if (!course.getInstructor().equals(instructor.getUsername())
                && instructor.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Bạn không có quyền sửa khóa học này!"));
        }

        if (body.containsKey("title"))       course.setTitle((String) body.get("title"));
        if (body.containsKey("description")) course.setDescription((String) body.get("description"));
        if (body.containsKey("credits"))     course.setCredits(Integer.valueOf(body.get("credits").toString()));
        if (body.containsKey("status"))      course.setStatus(Course.CourseStatus.valueOf((String) body.get("status")));
        course.setUpdatedAt(LocalDateTime.now());

        courseRepo.save(course);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ── DELETE /api/instructor/courses/{id} ──
    @DeleteMapping("/courses/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id,
                                          Authentication auth) {
        User instructor = getInstructor(auth);

        Course course = courseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học!"));

        if (!course.getInstructor().equals(instructor.getUsername())
                && instructor.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Bạn không có quyền xóa khóa học này!"));
        }

        courseRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ── GET /api/instructor/courses/{id}/students ──
    // Trả về danh sách học viên + tổng số
    @GetMapping("/courses/{id}/students")
    public ResponseEntity<?> getStudents(@PathVariable Long id,
                                         Authentication auth) {
        getInstructor(auth); // verify quyền

        List<Map<String, Object>> students = enrollmentRepo.findByCourseIdWithUser(id)
                .stream()
                .map(e -> Map.<String, Object>of(
                        "id",           e.getUser().getId(),
                        "username",     e.getUser().getUsername(),
                        "email",        e.getUser().getEmail(),
                        "enrolledAt",   e.getEnrolledAt() != null ? e.getEnrolledAt().toString() : ""
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "total",    students.size(),
                "students", students
        ));
    }

    // ══════════════════════════════════════════
    // LESSON MANAGEMENT
    // ══════════════════════════════════════════

    // GET /api/instructor/courses/{id}/lessons
    @GetMapping("/courses/{id}/lessons")
    public ResponseEntity<?> getLessons(@PathVariable Long id, Authentication auth) {
        getInstructor(auth);
        List<Map<String, Object>> lessons = lessonRepo.findByCourseIdOrderByOrderNum(id)
                .stream()
                .map(l -> Map.<String, Object>of(
                        "id",       l.getId(),
                        "title",    l.getTitle(),
                        "content",  l.getContent() != null ? l.getContent() : "",
                        "orderNum", l.getOrderNum(),
                        "videoUrl", l.getVideoUrl() != null ? l.getVideoUrl() : "",
                        "createdAt", l.getCreatedAt() != null ? l.getCreatedAt().toString() : ""
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(lessons);
    }

    // POST /api/instructor/courses/{id}/lessons
    @PostMapping("/courses/{id}/lessons")
    public ResponseEntity<?> createLesson(@PathVariable Long id,
                                          @RequestBody Map<String, Object> body,
                                          Authentication auth) {
        getInstructor(auth);
        Course course = courseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học!"));

        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        lesson.setTitle((String) body.get("title"));
        lesson.setContent((String) body.getOrDefault("content", ""));
        lesson.setVideoUrl((String) body.getOrDefault("videoUrl", ""));
        Object orderNum = body.get("orderNum");
        lesson.setOrderNum(orderNum != null ? Integer.valueOf(orderNum.toString()) : 1);

        Lesson saved = lessonRepo.save(lesson);
        return ResponseEntity.ok(Map.of(
                "id",       saved.getId(),
                "title",    saved.getTitle(),
                "orderNum", saved.getOrderNum()
        ));
    }

    // PUT /api/instructor/lessons/{lessonId}
    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<?> updateLesson(@PathVariable Long lessonId,
                                          @RequestBody Map<String, Object> body,
                                          Authentication auth) {
        User instructor = getInstructor(auth);
        Lesson lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học!"));

        // Kiểm tra lesson thuộc course của instructor
        if (!lesson.getCourse().getInstructor().equals(instructor.getUsername())
                && instructor.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("error", "Không có quyền!"));
        }

        if (body.containsKey("title"))    lesson.setTitle((String) body.get("title"));
        if (body.containsKey("content"))  lesson.setContent((String) body.get("content"));
        if (body.containsKey("videoUrl")) lesson.setVideoUrl((String) body.get("videoUrl"));
        if (body.containsKey("orderNum")) lesson.setOrderNum(Integer.valueOf(body.get("orderNum").toString()));

        lessonRepo.save(lesson);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // DELETE /api/instructor/lessons/{lessonId}
    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<?> deleteLesson(@PathVariable Long lessonId, Authentication auth) {
        User instructor = getInstructor(auth);
        Lesson lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học!"));

        if (!lesson.getCourse().getInstructor().equals(instructor.getUsername())
                && instructor.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("error", "Không có quyền!"));
        }

        lessonRepo.deleteById(lessonId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}