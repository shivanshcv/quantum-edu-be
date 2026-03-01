package com.quantum.edu.catalogue.repository;

import com.quantum.edu.catalogue.domain.ProductModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductModuleRepository extends JpaRepository<ProductModule, Long> {

    List<ProductModule> findByProductIdOrderByOrderIndexAsc(Long productId);
}
