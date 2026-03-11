package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.dto.AuthResponse;
import com.example.course.coursemanagement.dto.LoginRequest;
import com.example.course.coursemanagement.dto.RegisterRequest;
import com.example.course.coursemanagement.entity.User;
import com.example.course.coursemanagement.security.JwtUtil;
import com.example.course.coursemanagement.service.UserService;
import jakarta.validation.Valid;
import com.example.course.coursemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    // ====== ĐĂNG KÝ ======
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword()) // UserService sẽ hash
                .build();

        User savedUser = userService.createUser(newUser);

        // Tạo token ngay sau khi register — dùng username
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .role(savedUser.getRole().name())
                .message("Đăng ký thành công!")
                .build());
    }

    // ====== ĐĂNG NHẬP BẰNG USERNAME ======
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Xác thực username + password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),   // ← username, không phải email
                            request.getPassword()
                    )
            );
        } catch (DisabledException e) {
            return ResponseEntity.status(403).body(
                    AuthResponse.builder().message("Tài khoản của bạn đã bị khóa bởi quản trị viên!").build()
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(
                    AuthResponse.builder().message("Tên đăng nhập hoặc mật khẩu không đúng!").build()
            );
        }

        // Load user và tạo JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        User user = userService.getUserByUsername(request.getUsername());

        // Lấy avatar từ profile nếu có
        String avatar = userRepository.findByIdWithProfile(user.getId())
                .map(u -> u.getProfile() != null ? u.getProfile().getAvatar() : null)
                .orElse(null);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .avatar(avatar)
                .message("Đăng nhập thành công!")
                .build());
    }
}