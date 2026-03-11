package com.example.course.coursemanagement.controller;

import com.example.course.coursemanagement.dto.LeaderboardDTO;
import com.example.course.coursemanagement.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    // GET /api/leaderboard — toàn bộ bảng xếp hạng
    @GetMapping
    public ResponseEntity<List<LeaderboardDTO>> getLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getLeaderboard());
    }

    // GET /api/leaderboard/me?userId= — vị trí của user hiện tại
    @GetMapping("/me")
    public ResponseEntity<?> getMyRank(@RequestParam Long userId) {
        return leaderboardService.getLeaderboard().stream()
                .filter(d -> d.getUserId().equals(userId))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}