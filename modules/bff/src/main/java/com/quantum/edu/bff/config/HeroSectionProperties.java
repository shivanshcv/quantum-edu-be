package com.quantum.edu.bff.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "bff.home.hero")
@Getter
@Setter
public class HeroSectionProperties {
    private String title;
    private Headline headline;
    private String subtitle;
    private String badge;
    private Image image;
    private List<Stat> stats;
    private List<FloatingCard> floatingCards;
    private List<Cta> ctas;

    @Getter
    @Setter
    public static class Headline {
        private String line1;
        private String line2;
        private String highlightWord;
    }

    @Getter
    @Setter
    public static class Image {
        private String src;
        private String alt;
    }

    @Getter
    @Setter
    public static class Stat {
        private String id;
        private String value;
        private String label;
        private String icon;
    }

    @Getter
    @Setter
    public static class FloatingCard {
        private String id;
        private String title;
        private String subtitle;
        private String position;
    }

    @Getter
    @Setter
    public static class Cta {
        private String label;
        private String url;
        private String variant;
        private String type;
    }
}
