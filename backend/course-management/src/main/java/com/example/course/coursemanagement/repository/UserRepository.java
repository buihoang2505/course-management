package com.example.course.coursemanagement.repository;

import com.example.course.coursemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm user theo email (dùng cho login)
    Optional<User> findByEmail(String email);

    // Tìm user theo username
    Optional<User> findByUsername(String username);

    // Kiểm tra email đã tồn tại chưa (dùng khi register)
    boolean existsByEmail(String email);

    // Kiểm tra username đã tồn tại chưa
    boolean existsByUsername(String username);

    // Tìm user kèm theo profile (tránh N+1 query)
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.id = :id")
    Optional<User> findByIdWithProfile(Long id);
}