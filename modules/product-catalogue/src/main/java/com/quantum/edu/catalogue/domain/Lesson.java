package com.quantum.edu.catalogue.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "lesson")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_content_id", nullable = false, unique = true)
    private ProductContent productContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type", nullable = false, length = 10)
    private LessonType lessonType;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6)")
    private Instant createdAt;

    protected Lesson() {
    }

    public Lesson(ProductContent productContent, LessonType lessonType) {
        this.productContent = productContent;
        this.lessonType = lessonType;
    }

    public Long getId() { return id; }
    public ProductContent getProductContent() { return productContent; }
    public LessonType getLessonType() { return lessonType; }
    public String getVideoUrl() { return videoUrl; }
    public String getPdfUrl() { return pdfUrl; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public Instant getCreatedAt() { return createdAt; }

    public void setLessonType(LessonType lessonType) { this.lessonType = lessonType; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
}
