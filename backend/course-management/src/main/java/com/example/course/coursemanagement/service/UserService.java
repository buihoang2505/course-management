package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.entity.Role;
import com.example.course.coursemanagement.entity.User;
import com.example.course.coursemanagement.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService    emailService;
    private final QuizAttemptRepository attemptRepository;
    private final LessonCompletionRepository completionRepository;
    private final CertificateRepository certificateRepository;
    private final NotificationRepository notificationRepository;

    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail()))
            throw new IllegalArgumentException("Email đã được sử dụng!");
        if (userRepository.existsByUsername(user.getUsername()))
            throw new IllegalArgumentException("Username đã được sử dụng!");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        try { emailService.sendWelcome(saved.getUsername(), saved.getEmail()); }
        catch (Exception e) { log.warn("Failed to enqueue welcome email: {}", e.getMessage()); }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() { return userRepository.findAll(); }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user: " + id));
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user: " + username));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user: " + email));
    }

    public User updateUser(Long id, User updatedUser) {
        User existing = getUserById(id);
        existing.setUsername(updatedUser.getUsername());
        existing.setEmail(updatedUser.getEmail());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank())
            existing.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        return userRepository.save(existing);
    }

    // ── Đổi role (chỉ dành cho STUDENT / INSTRUCTOR / BANNED) ──
    // ADMIN không được phép dùng endpoint này để trao quyền ADMIN
    public User changeRole(Long id, Role newRole) {
        User user = getUserById(id);

        // Không cho phép trao quyền ADMIN qua endpoint này
        if (newRole == Role.ADMIN)
            throw new IllegalArgumentException("Không thể trao quyền ADMIN qua endpoint này!");

        // ADMIN chỉ có thể hạ cấp qua downgradeAdmin()
        if (user.getRole() == Role.ADMIN)
            throw new IllegalStateException("Không thể thay đổi role của ADMIN! Dùng endpoint /downgrade.");

        user.setRole(newRole);
        log.info("Role changed: userId={} username={} → {}", id, user.getUsername(), newRole);
        return userRepository.save(user);
    }

    // ── Hạ cấp ADMIN → STUDENT (chức năng riêng, kiểm soát chặt) ──
    public User downgradeAdmin(Long id) {
        User user = getUserById(id);
        if (user.getRole() != Role.ADMIN)
            throw new IllegalStateException("User này không phải ADMIN!");

        // Bảo vệ ADMIN cuối cùng — không cho hạ cấp nếu chỉ còn 1
        long adminCount = userRepository.countByRole(Role.ADMIN);
        if (adminCount <= 1)
            throw new IllegalStateException("Không thể hạ cấp! Hệ thống phải có ít nhất 1 ADMIN.");

        user.setRole(Role.STUDENT);
        log.info("ADMIN downgraded to STUDENT: userId={} username={}", id, user.getUsername());
        return userRepository.save(user);
    }

    // ── Ban user ──
    public User banUser(Long id) {
        User user = getUserById(id);
        if (user.getRole() == Role.ADMIN)
            throw new IllegalStateException("Không thể ban tài khoản ADMIN!");
        user.setRole(Role.BANNED);
        log.info("User BANNED: userId={} username={}", id, user.getUsername());
        return userRepository.save(user);
    }

    // ── Unban user → trả về STUDENT ──
    public User unbanUser(Long id) {
        User user = getUserById(id);
        if (user.getRole() != Role.BANNED)
            throw new IllegalStateException("User này không bị ban!");
        user.setRole(Role.STUDENT);
        log.info("User UNBANNED: userId={} username={}", id, user.getUsername());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        attemptRepository.deleteByUserId(id);
        completionRepository.deleteByUserId(id);
        certificateRepository.deleteByUserId(id);
        notificationRepository.deleteByUserId(id);
        userRepository.delete(user);
        log.info("Deleted user id={} username={}", id, user.getUsername());
    }
}