package com.quantum.edu.catalogue.repository;

import com.quantum.edu.catalogue.domain.ProductContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductContentRepository extends JpaRepository<ProductContent, Long> {

    List<ProductContent> findByProductIdOrderByOrderIndexAsc(Long productId);

    boolean existsByProductIdAndOrderIndex(Long productId, int orderIndex);

    @Query("SELECT c.product.id FROM ProductContent c WHERE c.id = :contentId")
    Optional<Long> findProductIdByContentId(@Param("contentId") Long contentId);
}
