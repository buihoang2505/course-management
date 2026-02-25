package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.dto.AuthResponse;
import com.example.course.coursemanagement.dto.LoginRequest;
import com.example.course.coursemanagement.dto.RegisterRequest;
import com.example.course.coursemanagement.entity.User;
import com.example.course.coursemanagement.security.JwtUtil;
import com.example.course.coursemanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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

    // ====== ĐĂNG KÝ ======
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        // Tạo user mới
        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword()) // UserService sẽ hash
                .build();

        User savedUser = userService.createUser(newUser);

        // Tạo token ngay sau khi register
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(AuthResponse.builder()
                .id(savedUser.getId())
                .token(token)
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .role(savedUser.getRole().name())
                .message("Đăng ký thành công!")
                .build());
    }

    // ====== ĐĂNG NHẬP ======
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Xác thực email + password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(
                    AuthResponse.builder().message("Email hoặc password không đúng!").build()
            );
        }

        // Load user và tạo JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        User user = userService.getUserByEmail(request.getEmail());

        return ResponseEntity.ok(AuthResponse.builder()
                .id(user.getId())
                .token(token)
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .message("Đăng nhập thành công!")
                .build());
    }
}