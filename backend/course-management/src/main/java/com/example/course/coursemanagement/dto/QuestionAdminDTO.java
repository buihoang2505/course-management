package com.example.course.coursemanagement.dto;

import com.example.course.coursemanagement.entity.Question;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class QuestionAdminDTO {
    private Long   id;
    private String content;
    private String explanation;
    private Integer orderNum;
    private Integer points;
    private List<ChoiceAdminItem> choices;

    @Data
    public static class ChoiceAdminItem {
        private Long    id;
        private String  content;
        private boolean isCorrect;
    }

    public static QuestionAdminDTO from(Question q) {
        QuestionAdminDTO dto = new QuestionAdminDTO();
        dto.setId(q.getId());
        dto.setContent(q.getContent());
        dto.setExplanation(q.getExplanation());
        dto.setOrderNum(q.getOrderNum());
        dto.setPoints(q.getPoints());
        dto.setChoices(q.getChoices().stream().map(c -> {
            ChoiceAdminItem item = new ChoiceAdminItem();
            item.setId(c.getId());
            item.setContent(c.getContent());
            item.setCorrect(Boolean.TRUE.equals(c.getIsCorrect()));
            return item;
        }).collect(Collectors.toList()));
        return dto;
    }
}