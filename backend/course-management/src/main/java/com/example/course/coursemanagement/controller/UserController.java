package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.dto.UserDTO;
import com.example.course.coursemanagement.entity.Role;
import com.example.course.coursemanagement.entity.User;
import com.example.course.coursemanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream().map(UserDTO::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(UserDTO.from(userService.getUserById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(UserDTO.from(userService.updateUser(id, user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // PATCH /api/users/{id}/role?role=STUDENT|INSTRUCTOR|BANNED
    // Không cho phép role=ADMIN — dùng endpoint riêng nếu cần hạ cấp
    @PatchMapping("/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable Long id, @RequestParam String role) {
        try {
            Role r = Role.valueOf(role.toUpperCase());
            // Chặn trao quyền ADMIN qua endpoint này
            if (r == Role.ADMIN)
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Không thể trao quyền ADMIN qua endpoint này!"));
            return ResponseEntity.ok(UserDTO.from(userService.changeRole(id, r)));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PATCH /api/users/{id}/downgrade — hạ cấp ADMIN → STUDENT
    @PatchMapping("/{id}/downgrade")
    public ResponseEntity<?> downgradeAdmin(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(UserDTO.from(userService.downgradeAdmin(id)));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PATCH /api/users/{id}/ban
    @PatchMapping("/{id}/ban")
    public ResponseEntity<?> banUser(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(UserDTO.from(userService.banUser(id)));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PATCH /api/users/{id}/username — đổi username
    @PatchMapping("/{id}/username")
    public ResponseEntity<?> changeUsername(@PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "").trim();
        if (username.isBlank() || !username.matches("^[a-zA-Z0-9_]{3,30}$"))
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username không hợp lệ (3–30 ký tự, chỉ dùng chữ/số/_)"));
        try {
            User user = userService.changeUsername(id, username);
            return ResponseEntity.ok(Map.of(
                    "message",  "Đổi tên đăng nhập thành công!",
                    "username", user.getUsername()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PATCH /api/users/{id}/unban — trả về STUDENT
    @PatchMapping("/{id}/unban")
    public ResponseEntity<?> unbanUser(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(UserDTO.from(userService.unbanUser(id)));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}