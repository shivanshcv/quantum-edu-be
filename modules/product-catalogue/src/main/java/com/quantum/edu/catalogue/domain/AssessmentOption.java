package com.quantum.edu.catalogue.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "assessment_option", indexes = {
        @Index(name = "idx_assessment_option_question_id", columnList = "question_id")
})
public class AssessmentOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private AssessmentQuestion question;

    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6)")
    private Instant createdAt;

    protected AssessmentOption() {
    }

    public AssessmentOption(AssessmentQuestion question, String optionText, boolean correct) {
        this.question = question;
        this.optionText = optionText;
        this.correct = correct;
    }

    public Long getId() { return id; }
    public AssessmentQuestion getQuestion() { return question; }
    public String getOptionText() { return optionText; }
    public boolean isCorrect() { return correct; }
    public Instant getCreatedAt() { return createdAt; }

    public void setOptionText(String optionText) { this.optionText = optionText; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}
