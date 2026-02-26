package com.quantum.edu.catalogue.api;

import com.quantum.edu.catalogue.domain.Product;

import java.util.Optional;

public interface ProductCatalogueApi {

    boolean existsById(Long productId);

    boolean isPublished(Long productId);

    Optional<Product> getProduct(Long productId);
}
