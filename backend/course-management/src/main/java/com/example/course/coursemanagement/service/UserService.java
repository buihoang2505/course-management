package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.entity.Role;
import com.example.course.coursemanagement.entity.User;
import com.example.course.coursemanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ====== TẠO USER MỚI (REGISTER) ======
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username đã được sử dụng!");
        }
        // ← Thêm dòng này:
        if (user.getRole() == null) {
            user.setRole(Role.STUDENT);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // ====== LẤY TẤT CẢ USER ======
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ====== LẤY USER THEO ID ======
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user với id: " + id));
    }

    // ====== LẤY USER THEO EMAIL ======
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user với email: " + email));
    }

    // ====== CẬP NHẬT USER ======
    public User updateUser(Long id, User updatedUser) {
        User existing = getUserById(id);
        existing.setUsername(updatedUser.getUsername());
        existing.setEmail(updatedUser.getEmail());
        // Chỉ đổi password nếu có truyền vào
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        return userRepository.save(existing);
    }

    // ====== XÓA USER ======
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}