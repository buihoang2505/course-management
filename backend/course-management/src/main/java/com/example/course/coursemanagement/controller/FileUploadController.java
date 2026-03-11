package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.entity.Profile;
import com.example.course.coursemanagement.entity.User;
import com.example.course.coursemanagement.repository.LessonRepository;
import com.example.course.coursemanagement.repository.UserRepository;
import com.example.course.coursemanagement.service.FileUploadService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Controller xử lý upload file.
 *
 * POST /api/upload/avatar?userId=1   — upload ảnh đại diện
 * POST /api/upload/video?lessonId=5  — upload video bài học
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final UserRepository    userRepository;
    private final LessonRepository  lessonRepository;

    // ══════════════════════════════════════════════════════════
    //  UPLOAD AVATAR
    //  POST /api/upload/avatar?userId=1
    //  Content-Type: multipart/form-data
    //  Body field name: file
    // ══════════════════════════════════════════════════════════
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam Long userId,
            @RequestParam("file") MultipartFile file) {
        try {
            // Tìm user
            User user = userRepository.findByIdWithProfile(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            Profile profile = user.getProfile();
            if (profile == null) {
                profile = new Profile();
                profile.setUser(user);
                user.setProfile(profile);
            }

            // Xóa avatar cũ trên Cloudinary (nếu có)
            if (profile.getAvatar() != null && profile.getAvatar().contains("cloudinary")) {
                fileUploadService.deleteFile(profile.getAvatar(), "image");
            }

            // Upload mới
            String avatarUrl = fileUploadService.uploadAvatar(file);
            profile.setAvatar(avatarUrl);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message",   "Upload avatar thành công!",
                    "avatarUrl", avatarUrl
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Avatar upload failed for userId={}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload thất bại, vui lòng thử lại!"));
        }
    }

    // ══════════════════════════════════════════════════════════
    //  UPLOAD VIDEO
    //  POST /api/upload/video?lessonId=5
    //  Content-Type: multipart/form-data
    //  Body field name: file
    // ══════════════════════════════════════════════════════════
    @PostMapping("/video")
    public ResponseEntity<?> uploadVideo(
            @RequestParam Long lessonId,
            @RequestParam("file") MultipartFile file) {
        try {
            var lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));

            // Xóa video cũ trên Cloudinary (nếu là Cloudinary URL)
            if (lesson.getVideoUrl() != null
                    && lesson.getVideoUrl().contains("cloudinary")) {
                fileUploadService.deleteFile(lesson.getVideoUrl(), "video");
            }

            // Upload mới
            String videoUrl = fileUploadService.uploadVideo(file);
            lesson.setVideoUrl(videoUrl);
            lessonRepository.save(lesson);

            return ResponseEntity.ok(Map.of(
                    "message",  "Upload video thành công!",
                    "videoUrl", videoUrl
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Video upload failed for lessonId={}: {}", lessonId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload thất bại, vui lòng thử lại!"));
        }
    }
}