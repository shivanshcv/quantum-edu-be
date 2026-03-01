package com.quantum.edu.catalogue.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_module", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_module_order", columnNames = {"product_id", "order_index"})
}, indexes = {
        @Index(name = "idx_product_module_product_id", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<ProductContent> contents = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6)")
    private Instant createdAt;
}
