package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.dto.AdminStatsDTO;
import com.example.course.coursemanagement.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService statsService;

    // GET /api/admin/stats
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(statsService.getStats());
    }
}