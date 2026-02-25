package com.example.course.coursemanagement;

import com.example.course.coursemanagement.entity.*;
import com.example.course.coursemanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Xóa @Component khi không cần seed nữa
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Chỉ seed khi DB trống
        if (userRepository.count() > 0) {
            log.info("DB đã có dữ liệu, bỏ qua seed.");
            return;
        }

        log.info("=== Bắt đầu seed dữ liệu mẫu ===");

        // 1. Tạo Admin
        User admin = userRepository.save(User.builder()
                .username("admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("Admin@1234"))
                .role(Role.ADMIN)
                .build());

        // 2. Tạo Student
        User student = userRepository.save(User.builder()
                .username("student1")
                .email("student1@example.com")
                .password(passwordEncoder.encode("Student@1234"))
                .role(Role.STUDENT)
                .build());

        // 3. Tạo Profile cho student
        Profile profile = Profile.builder()
                .fullName("Nguyễn Văn A")
                .bio("Sinh viên năm 3")
                .phone("0901234567")
                .user(student)
                .build();
        student.setProfile(profile);
        userRepository.save(student);

        // 4. Tạo Course
        Course course = courseRepository.save(Course.builder()
                .title("Lập trình Java cơ bản")
                .description("Khóa học Java dành cho người mới bắt đầu")
                .credits(3)
                .instructor("Thầy Nguyễn")
                .status(Course.CourseStatus.ACTIVE)
                .build());

        // 5. Tạo Lessons
        lessonRepository.save(Lesson.builder()
                .title("Bài 1: Giới thiệu Java")
                .content("Java là ngôn ngữ lập trình hướng đối tượng...")
                .orderNum(1)
                .course(course)
                .build());

        lessonRepository.save(Lesson.builder()
                .title("Bài 2: Biến và kiểu dữ liệu")
                .content("Các kiểu dữ liệu trong Java: int, String, boolean...")
                .orderNum(2)
                .course(course)
                .build());

        // 6. Đăng ký khóa học
        Enrollment enrollment = enrollmentRepository.save(Enrollment.builder()
                .user(student)
                .course(course)
                .status(Enrollment.EnrollmentStatus.ACTIVE)
                .build());

        // 7. Nhập điểm
        gradeRepository.save(Grade.builder()
                .score(8.5)
                .feedback("Học tốt, cần cố gắng thêm ở phần OOP")
                .enrollment(enrollment)
                .build());

        log.info("=== Seed xong! Đã tạo {} users, {} courses ===",
                userRepository.count(), courseRepository.count());
    }
}