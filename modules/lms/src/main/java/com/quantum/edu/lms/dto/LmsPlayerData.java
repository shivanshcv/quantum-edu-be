package com.quantum.edu.lms.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LmsPlayerData {

    private String courseSlug;
    private String courseTitle;
    private String subtitle;
    private String instructor;
    private int completedLessons;
    private int totalLessons;
    private int progressPercentage;
    private List<ModuleData> modules;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ModuleData {
        private Long id;
        private String title;
        private boolean locked;
        private List<LessonData> lessons;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LessonData {
        private Long contentId;
        private String title;
        private String description;
        private int durationMinutes;
        private String status; // completed, in_progress, not_started, locked
        private String url; // Media URL: video, pdf, or ppt based on type
        private String moduleType; // LESSON, QUIZ
        private String type; // VIDEO, PDF, PPT (for LESSON); null for QUIZ
        private AssessmentData assessment; // For QUIZ only: questions and options for display
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssessmentData {
        private int passPercentage;
        private List<QuestionData> questions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionData {
        private Long id;
        private String questionText;
        private List<OptionData> options;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionData {
        private Long id;
        private String optionText;
    }
}
