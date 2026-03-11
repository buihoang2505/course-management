package com.example.course.coursemanagement.dto;

import com.example.course.coursemanagement.entity.Notification;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private String type;
    private String title;
    private String message;
    private boolean read;
    private String link;
    private LocalDateTime createdAt;

    public static NotificationDTO from(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setType(n.getType() != null ? n.getType().name() : "SYSTEM");
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setRead(n.isRead());
        dto.setLink(n.getLink());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}