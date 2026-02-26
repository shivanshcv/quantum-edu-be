package com.quantum.edu.catalogue.repository;

import com.quantum.edu.catalogue.domain.ProductContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductContentRepository extends JpaRepository<ProductContent, Long> {

    List<ProductContent> findByProductIdOrderByOrderIndexAsc(Long productId);

    boolean existsByProductIdAndOrderIndex(Long productId, int orderIndex);
}
