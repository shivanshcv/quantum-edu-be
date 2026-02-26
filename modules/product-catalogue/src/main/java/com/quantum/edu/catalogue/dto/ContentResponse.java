package com.quantum.edu.catalogue.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentResponse {

    private Long id;
    private String contentType;
    private String title;
    private int orderIndex;
    private boolean mandatory;
    private LessonDetail lesson;
    private AssessmentDetail assessment;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LessonDetail {
        private Long id;
        private String lessonType;
        private String videoUrl;
        private String pdfUrl;
        private Integer durationSeconds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssessmentDetail {
        private Long id;
        private int passPercentage;
        private int questionCount;
    }
}
