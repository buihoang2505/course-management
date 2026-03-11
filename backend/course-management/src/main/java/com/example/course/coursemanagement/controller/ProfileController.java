package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.entity.Profile;
import com.example.course.coursemanagement.entity.User;
import com.example.course.coursemanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    // GET /api/users/{id}/profile
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

    // PUT /api/users/{id}/profile
    @PutMapping("/{id}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable Long id,
                                           @RequestBody Map<String, String> body) {
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
                "message", "Cập nhật thành công!",
                "fullName", p.getFullName() != null ? p.getFullName() : "",
                "phone",    p.getPhone()    != null ? p.getPhone()    : "",
                "bio",      p.getBio()      != null ? p.getBio()      : ""
        ));
    }
}