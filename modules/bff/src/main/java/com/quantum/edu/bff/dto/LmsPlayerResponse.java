package com.quantum.edu.bff.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LmsPlayerResponse {

    private String courseSlug;
    private String courseTitle;
    private String subtitle;
    private String instructor;
    private int completedLessons;
    private int totalLessons;
    private int progressPercentage;
    private List<ModuleResponse> modules;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ModuleResponse {
        private Long id; // product_module id; -1 for synthetic intro module
        private String title;
        @JsonProperty("isLocked")
        private boolean locked;
        private List<LessonResponse> lessons;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LessonResponse {
        private Long id; // product_content id; use this as contentId in POST /lms/lessons/{contentId}/complete
        private String title;
        private String description;
        private int durationMinutes;
        private String status;
        private String url; // Media URL: video, pdf, or ppt based on type
        private String moduleType; // LESSON, QUIZ
        private String type; // VIDEO, PDF, PPT (for LESSON); null for QUIZ
        private AssessmentResponse assessment; // For QUIZ only: questions and options (no correct answers)
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssessmentResponse {
        private int passPercentage;
        private List<QuestionResponse> questions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionResponse {
        private Long id;
        private String questionText;
        private List<OptionResponse> options;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionResponse {
        private Long id;
        private String optionText;
    }
}
