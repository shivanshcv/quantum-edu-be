package com.quantum.edu.catalogue.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "product", indexes = {
        @Index(name = "idx_product_is_published", columnList = "is_published"),
        @Index(name = "idx_product_published_featured", columnList = "is_published, is_featured")
})
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "short_description", nullable = false, columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "long_description", nullable = false, columnDefinition = "TEXT")
    private String longDescription;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "preview_video_url", length = 500)
    private String previewVideoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 20)
    private DifficultyLevel difficultyLevel;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "is_published", nullable = false)
    private boolean published = false;

    @Column(name = "is_featured", nullable = false)
    private boolean featured = false;

    @Column(name = "is_free", nullable = false)
    private boolean free = false;

    @Convert(converter = ProductAttributesConverter.class)
    @Column(columnDefinition = "json")
    private ProductAttributes attributes;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"),
            indexes = @Index(name = "idx_product_category_category_id", columnList = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<ProductContent> contents = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<ProductModule> modules = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6)")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6) on update current_timestamp(6)")
    private Instant updatedAt;

    public Product(String title, String slug, String shortDescription, String longDescription,
                   BigDecimal price, DifficultyLevel difficultyLevel) {
        this.title = title;
        this.slug = slug;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.price = price;
        this.difficultyLevel = difficultyLevel;
    }

    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}
