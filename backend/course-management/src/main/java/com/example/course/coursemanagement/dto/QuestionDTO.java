package com.example.course.coursemanagement.dto;

import com.example.course.coursemanagement.entity.Question;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

// Dùng khi học sinh làm bài — KHÔNG có đáp án đúng
@Data
public class QuestionDTO {
    private Long         id;
    private String       content;
    private Integer      orderNum;
    private Integer      points;
    private List<ChoiceDTO> choices;

    public static QuestionDTO from(Question q) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(q.getId());
        dto.setContent(q.getContent());
        dto.setOrderNum(q.getOrderNum());
        dto.setPoints(q.getPoints());
        dto.setChoices(q.getChoices().stream()
                .map(ChoiceDTO::from)
                .collect(Collectors.toList()));
        return dto;
    }
}