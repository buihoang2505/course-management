package com.example.course.coursemanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "grades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DecimalMin(value = "0.0", message = "Điểm không được nhỏ hơn 0")
    @DecimalMax(value = "10.0", message = "Điểm không được lớn hơn 10")
    private Double score;

    @Column(columnDefinition = "TEXT")
    private String feedback; // Nhận xét của giảng viên

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime gradedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ============ RELATIONSHIPS ============

    // @OneToOne – Grade thuộc về 1 Enrollment cụ thể
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private Enrollment enrollment;
}