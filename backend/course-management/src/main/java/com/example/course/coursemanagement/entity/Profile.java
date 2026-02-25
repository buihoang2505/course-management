package com.example.course.coursemanagement.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 100, message = "Họ tên không được quá 100 ký tự")
    @Column(length = 100)
    private String fullName;

    @Column(length = 255)
    private String avatar; // URL ảnh đại diện

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Size(max = 20)
    @Column(length = 20)
    private String phone;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ============ RELATIONSHIPS ============

    // @OneToOne – Profile thuộc về 1 User duy nhất
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}