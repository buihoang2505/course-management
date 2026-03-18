package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.entity.Profile;
import com.example.course.coursemanagement.entity.User;
import com.example.course.coursemanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    // GET /api/users/{id}/profile — public đọc được (hiển thị tên, avatar)
    @GetMapping("/{id}/profile")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        User user = userRepository.findByIdWithProfile(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Profile p = user.getProfile();
        return ResponseEntity.ok(Map.of(
                "fullName", p != null && p.getFullName() != null ? p.getFullName() : "",
                "phone",    p != null && p.getPhone()    != null ? p.getPhone()    : "",
                "bio",      p != null && p.getBio()      != null ? p.getBio()      : "",
                "avatar",   p != null && p.getAvatar()   != null ? p.getAvatar()   : ""
        ));
    }

    // PUT /api/users/{id}/profile — chỉ chính chủ hoặc ADMIN mới sửa được
    @PutMapping("/{id}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable Long id,
                                           @RequestBody Map<String, String> body,
                                           @AuthenticationPrincipal UserDetails principal) {
        // Ownership check
        User requester = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!requester.getId().equals(id) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Khong co quyen sua profile cua nguoi khac!"));
        }

        User user = userRepository.findByIdWithProfile(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Profile p = user.getProfile();
        if (p == null) {
            p = new Profile();
            p.setUser(user);
            user.setProfile(p);
        }

        if (body.containsKey("fullName")) p.setFullName(body.get("fullName").trim());
        if (body.containsKey("phone"))    p.setPhone(body.get("phone").trim());
        if (body.containsKey("bio"))      p.setBio(body.get("bio").trim());

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message",  "Cap nhat thanh cong!",
                "fullName", p.getFullName() != null ? p.getFullName() : "",
                "phone",    p.getPhone()    != null ? p.getPhone()    : "",
                "bio",      p.getBio()      != null ? p.getBio()      : ""
        ));
    }
}