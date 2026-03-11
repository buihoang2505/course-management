package com.example.course.coursemanagement.dto;

import com.example.course.coursemanagement.entity.Certificate;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CertificateDTO {

    private Long          id;
    private String        certificateCode;
    private String        courseTitle;
    private String        studentName;
    private Double        averageQuizScore;
    private Integer       totalLessons;
    private LocalDateTime issuedAt;
    private Long          courseId;
    private Long          userId;

    public static CertificateDTO from(Certificate c) {
        CertificateDTO dto = new CertificateDTO();
        dto.setId(c.getId());
        dto.setCertificateCode(c.getCertificateCode());
        dto.setCourseTitle(c.getCourseTitle());
        dto.setStudentName(c.getStudentName());
        dto.setAverageQuizScore(c.getAverageQuizScore());
        dto.setTotalLessons(c.getTotalLessons());
        dto.setIssuedAt(c.getIssuedAt());
        dto.setCourseId(c.getCourse() != null ? c.getCourse().getId() : null);
        dto.setUserId(c.getUser() != null ? c.getUser().getId() : null);
        return dto;
    }
}