package com.quantum.edu.catalogue.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assessment_question", indexes = {
        @Index(name = "idx_assessment_question_assessment_id", columnList = "assessment_id")
})
public class AssessmentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssessmentOption> options = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6)")
    private Instant createdAt;

    protected AssessmentQuestion() {
    }

    public AssessmentQuestion(Assessment assessment, String questionText) {
        this.assessment = assessment;
        this.questionText = questionText;
    }

    public Long getId() { return id; }
    public Assessment getAssessment() { return assessment; }
    public String getQuestionText() { return questionText; }
    public List<AssessmentOption> getOptions() { return options; }
    public Instant getCreatedAt() { return createdAt; }

    public void setQuestionText(String questionText) { this.questionText = questionText; }
}
