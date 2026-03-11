package com.example.course.coursemanagement.dto;

import com.example.course.coursemanagement.entity.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// ── ChoiceDTO: ẩn isCorrect khi trả cho học sinh ──────────
@Data
public class ChoiceDTO {
    private Long   id;
    private String content;
    // isCorrect chỉ trả về khi xem kết quả (không trả khi làm bài)

    public static ChoiceDTO from(Choice c) {
        ChoiceDTO dto = new ChoiceDTO();
        dto.setId(c.getId());
        dto.setContent(c.getContent());
        return dto;
    }
}