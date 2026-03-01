package com.quantum.edu.bff.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bff.my-learning")
@Getter
@Setter
@NoArgsConstructor
public class MyLearningProperties {
    private String badge;
    private String title;
    private String subtitle;
    private EmptyState emptyState = new EmptyState();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class EmptyState {
        private String icon = "book";
        private String title = "No active enrollments";
        private String message = "Your learning path is empty. Explore our catalog to find the program that fits your career goals.";
        private Cta cta = new Cta();

        @Getter
        @Setter
        @NoArgsConstructor
        public static class Cta {
            private String label = "FIND A PROGRAM";
            private String url = "/courses";
            private String variant = "primary";
            private String type = "button";
        }
    }
}
