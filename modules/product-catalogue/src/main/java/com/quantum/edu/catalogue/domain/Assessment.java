package com.quantum.edu.catalogue.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assessment")
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_content_id", nullable = false, unique = true)
    private ProductContent productContent;

    @Column(name = "pass_percentage", nullable = false)
    private int passPercentage;

    @OneToMany(mappedBy = "assessment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssessmentQuestion> questions = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6)")
    private Instant createdAt;

    protected Assessment() {
    }

    public Assessment(ProductContent productContent, int passPercentage) {
        this.productContent = productContent;
        this.passPercentage = passPercentage;
    }

    public Long getId() { return id; }
    public ProductContent getProductContent() { return productContent; }
    public int getPassPercentage() { return passPercentage; }
    public List<AssessmentQuestion> getQuestions() { return questions; }
    public Instant getCreatedAt() { return createdAt; }

    public void setPassPercentage(int passPercentage) { this.passPercentage = passPercentage; }
}
