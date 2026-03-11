package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.entity.Notification;
import com.example.course.coursemanagement.dto.NotificationDTO;
import com.example.course.coursemanagement.service.NotificationService;
import com.example.course.coursemanagement.repository.NotificationRepository;
import com.example.course.coursemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notifService;
    private final NotificationRepository notifRepo;
    private final UserRepository userRepo;

    // GET /api/notifications?userId=1
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAll(@RequestParam Long userId) {
        List<NotificationDTO> dtos = notifService.getUserNotifications(userId)
                .stream().map(NotificationDTO::from).toList();
        return ResponseEntity.ok(dtos);
    }

    // GET /api/notifications/unread-count?userId=1
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@RequestParam Long userId) {
        return ResponseEntity.ok(Map.of("count", notifService.countUnread(userId)));
    }

    // PATCH /api/notifications/{id}/read
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        notifService.markRead(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // PATCH /api/notifications/read-all?userId=1
    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllRead(@RequestParam Long userId) {
        notifService.markAllRead(userId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // DELETE /api/notifications/clear?userId=1
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearAll(@RequestParam Long userId) {
        notifService.clearAll(userId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ── ADMIN endpoints ──

    // GET /api/notifications/admin/all?page=0&size=30&type=&search= — phân trang
    @GetMapping("/admin/all")
    public ResponseEntity<?> adminGetAll(
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "30") int    size,
            @RequestParam(defaultValue = "")   String type,
            @RequestParam(defaultValue = "")   String search) {

        var pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        org.springframework.data.domain.Page<Notification> result;
        if (!type.isBlank() && !search.isBlank()) {
            result = notifRepo.findByTypeAndSearch(type, search.toLowerCase(), pageable);
        } else if (!type.isBlank()) {
            result = notifRepo.findByTypeContaining(type, pageable);
        } else if (!search.isBlank()) {
            result = notifRepo.findBySearch(search.toLowerCase(), pageable);
        } else {
            result = notifRepo.findAll(pageable);
        }
        List<NotificationDTO> dtos = result.getContent().stream()
                .map(NotificationDTO::from).toList();
        return ResponseEntity.ok(Map.of(
                "content",       dtos,
                "totalElements", result.getTotalElements(),
                "totalPages",    result.getTotalPages(),
                "page",          result.getNumber()
        ));
    }

    // DELETE /api/notifications/admin/clear-all — xóa tất cả thông báo hệ thống
    @DeleteMapping("/admin/clear-all")
    public ResponseEntity<?> adminClearAll() {
        long count = notifRepo.count();
        notifRepo.deleteAll();
        return ResponseEntity.ok(Map.of("deleted", count));
    }

    // DELETE /api/notifications/admin/{id} — admin xóa 1 thông báo
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> adminDelete(@PathVariable Long id) {
        notifRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // POST /api/notifications/admin/broadcast — gửi thông báo cho tất cả hoặc 1 user
    @PostMapping("/admin/broadcast")
    public ResponseEntity<?> broadcast(@RequestBody Map<String, Object> body) {
        String title   = (String) body.get("title");
        String message = (String) body.get("message");
        String link    = (String) body.getOrDefault("link", "/courses");
        Object targetId = body.get("userId"); // null = gửi tất cả

        if (targetId != null) {
            Long userId = Long.valueOf(targetId.toString());
            notifService.create(userId, Notification.Type.SYSTEM, title, message, link);
            return ResponseEntity.ok(Map.of("sent", 1));
        }

        // Broadcast tất cả user
        List<Long> userIds = userRepo.findAll().stream()
                .filter(u -> u.getRole().name().equals("STUDENT") || u.getRole().name().equals("INSTRUCTOR"))
                .map(u -> u.getId())
                .toList();
        userIds.forEach(uid -> notifService.create(uid, Notification.Type.SYSTEM, title, message, link));
        return ResponseEntity.ok(Map.of("sent", userIds.size()));
    }
}