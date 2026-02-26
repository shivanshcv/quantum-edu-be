package com.quantum.edu.catalogue.repository;

import com.quantum.edu.catalogue.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Product> findByPublishedTrue(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.published = true " +
            "AND (:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:difficulty IS NULL OR p.difficultyLevel = :difficulty)")
    Page<Product> searchPublished(
            @Param("search") String search,
            @Param("difficulty") Product.DifficultyLevel difficulty,
            Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p JOIN p.categories c WHERE p.published = true AND c.id = :categoryId")
    Page<Product> findPublishedByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    Optional<Product> findBySlugAndPublishedTrue(String slug);

    Optional<Product> findByIdAndPublishedTrue(Long id);
}
