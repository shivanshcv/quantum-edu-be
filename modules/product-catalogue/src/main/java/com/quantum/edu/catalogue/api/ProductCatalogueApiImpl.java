package com.quantum.edu.catalogue.api;

import com.quantum.edu.catalogue.domain.Product;
import com.quantum.edu.catalogue.repository.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProductCatalogueApiImpl implements ProductCatalogueApi {

    private final ProductRepository productRepository;

    public ProductCatalogueApiImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public boolean existsById(Long productId) {
        return productRepository.existsById(productId);
    }

    @Override
    public boolean isPublished(Long productId) {
        return productRepository.findById(productId)
                .map(Product::isPublished)
                .orElse(false);
    }

    @Override
    public Optional<Product> getProduct(Long productId) {
        return productRepository.findById(productId);
    }
}
