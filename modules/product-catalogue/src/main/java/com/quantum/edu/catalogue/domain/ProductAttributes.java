package com.quantum.edu.catalogue.domain;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAttributes {

    private String badge;
    private List<Highlight> highlights;
    private List<String> learningOutcomes;
    private InstructorInfo instructor;
    private CertificationInfo certification;
    private List<OutcomeHighlight> outcomeHighlights;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Highlight {
        private String icon;
        private String label;
        private String value;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InstructorInfo {
        private String name;
        private String role;
        private String imageUrl;
        private String bio;
        private List<String> credentials;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CertificationInfo {
        private String icon;
        private String title;
        private String description;
        private List<String> highlights;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OutcomeHighlight {
        private String title;
        private String description;
    }
}
