package com.quantum.edu.catalogue.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "product", indexes = {
        @Index(name = "idx_product_is_published", columnList = "is_published")
})
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

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6)")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6) on update current_timestamp(6)")
    private Instant updatedAt;

    protected Product() {
    }

    public Product(String title, String slug, String shortDescription, String longDescription,
                   BigDecimal price, DifficultyLevel difficultyLevel) {
        this.title = title;
        this.slug = slug;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.price = price;
        this.difficultyLevel = difficultyLevel;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public String getShortDescription() { return shortDescription; }
    public String getLongDescription() { return longDescription; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getDiscountPrice() { return discountPrice; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getPreviewVideoUrl() { return previewVideoUrl; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public boolean isPublished() { return published; }
    public Set<Category> getCategories() { return categories; }
    public List<ProductContent> getContents() { return contents; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setTitle(String title) { this.title = title; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setShortDescription(String s) { this.shortDescription = s; }
    public void setLongDescription(String s) { this.longDescription = s; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setDiscountPrice(BigDecimal p) { this.discountPrice = p; }
    public void setThumbnailUrl(String url) { this.thumbnailUrl = url; }
    public void setPreviewVideoUrl(String url) { this.previewVideoUrl = url; }
    public void setDifficultyLevel(DifficultyLevel d) { this.difficultyLevel = d; }
    public void setDurationMinutes(Integer m) { this.durationMinutes = m; }
    public void setPublished(boolean published) { this.published = published; }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}
