package com.example.course.coursemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên khóa học không được để trống")
    @Size(max = 200, message = "Tên khóa học không được quá 200 ký tự")
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Min(value = 1, message = "Số tín chỉ phải ít nhất là 1")
    private Integer credits;

    @Column(length = 100)
    private String instructor; // Tên giảng viên (đơn giản, có thể mở rộng sau)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status = CourseStatus.ACTIVE;

    // Giá khóa học (VNĐ). NULL hoặc 0 = miễn phí
    // KHÔNG dùng @Builder.Default — gây bug: Lombok reset giá trị từ DB về default
    @Column(name = "price", columnDefinition = "BIGINT DEFAULT 0")
    private Long price = 0L;

    // Tự động sync với price — true nếu price=0, false nếu price>0
    // KHÔNG dùng @Builder.Default — gây bug với Hibernate
    @Column(name = "is_free", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isFree = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ============ RELATIONSHIPS ============

    // @OneToMany – 1 Course có nhiều Lesson
    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("orderNum ASC")
    private List<Lesson> lessons = new ArrayList<>();

    // @OneToMany – 1 Course có nhiều Enrollment
    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enrollment> enrollments = new ArrayList<>();

    // ── Tự động sync isFree từ price trước mỗi lần save ──────────
    @PrePersist
    @PreUpdate
    public void syncIsFree() {
        this.isFree = (this.price == null || this.price == 0L);
    }

    // ============ ENUM ============

    public enum CourseStatus {
        ACTIVE, INACTIVE, DRAFT
    }
}