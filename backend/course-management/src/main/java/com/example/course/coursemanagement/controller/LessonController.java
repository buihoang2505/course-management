package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.entity.Course;
import com.example.course.coursemanagement.entity.Lesson;
import com.example.course.coursemanagement.repository.LessonRepository;
import com.example.course.coursemanagement.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonRepository lessonRepository;
    private final CourseService courseService;

    // GET /api/lessons/course/{courseId} – lấy bài học theo khóa học
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Lesson>> getLessonsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(lessonRepository.findByCourseIdOrderByOrderNumAsc(courseId));
    }

    // GET /api/lessons/{id} – lấy 1 bài học
    @GetMapping("/{id}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable Long id) {
        return ResponseEntity.ok(lessonRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Không tìm thấy bài học với id: " + id)));
    }

    // POST /api/lessons?courseId=1 – tạo bài học mới
    @PostMapping
    public ResponseEntity<Lesson> createLesson(@RequestParam Long courseId,
                                               @Valid @RequestBody Lesson lesson) {
        Course course = courseService.getCourseById(courseId);
        lesson.setCourse(course);
        return ResponseEntity.status(201).body(lessonRepository.save(lesson));
    }

    // PUT /api/lessons/{id} – cập nhật bài học
    @PutMapping("/{id}")
    public ResponseEntity<Lesson> updateLesson(@PathVariable Long id,
                                               @RequestBody Lesson updated) {
        Lesson existing = lessonRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Không tìm thấy bài học với id: " + id));
        existing.setTitle(updated.getTitle());
        existing.setContent(updated.getContent());
        existing.setOrderNum(updated.getOrderNum());
        existing.setVideoUrl(updated.getVideoUrl());
        return ResponseEntity.ok(lessonRepository.save(existing));
    }

    // DELETE /api/lessons/{id} – xóa bài học
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        lessonRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}