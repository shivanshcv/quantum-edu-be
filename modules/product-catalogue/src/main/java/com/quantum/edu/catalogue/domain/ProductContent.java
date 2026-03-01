package com.quantum.edu.catalogue.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "product_content", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_content_order", columnNames = {"product_id", "order_index"})
}, indexes = {
        @Index(name = "idx_product_content_product_id", columnList = "product_id"),
        @Index(name = "idx_product_content_module_id", columnList = "module_id")
})
@Getter
@Setter
@NoArgsConstructor
public class ProductContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private ProductModule module;

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

    public ProductContent(Product product, ContentType contentType, String title, int orderIndex) {
        this.product = product;
        this.contentType = contentType;
        this.title = title;
        this.orderIndex = orderIndex;
    }

    public enum ContentType {
        LESSON, ASSESSMENT
    }
}
