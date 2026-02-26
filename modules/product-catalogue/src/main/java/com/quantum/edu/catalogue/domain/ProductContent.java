package com.quantum.edu.catalogue.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "product_content", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_content_order", columnNames = {"product_id", "order_index"})
}, indexes = {
        @Index(name = "idx_product_content_product_id", columnList = "product_id")
})
public class ProductContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private ContentType contentType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "is_mandatory", nullable = false)
    private boolean mandatory = true;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6)")
    private Instant createdAt;

    protected ProductContent() {
    }

    public ProductContent(Product product, ContentType contentType, String title, int orderIndex) {
        this.product = product;
        this.contentType = contentType;
        this.title = title;
        this.orderIndex = orderIndex;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public ContentType getContentType() { return contentType; }
    public String getTitle() { return title; }
    public int getOrderIndex() { return orderIndex; }
    public boolean isMandatory() { return mandatory; }
    public Instant getCreatedAt() { return createdAt; }

    public void setTitle(String title) { this.title = title; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

    public enum ContentType {
        LESSON, ASSESSMENT
    }
}
