package com.quantum.edu.catalogue.service;

import com.quantum.edu.catalogue.domain.Product;
import com.quantum.edu.catalogue.domain.ProductAttributes;
import com.quantum.edu.catalogue.repository.ProductRepository;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductPdpService {

    private final ProductRepository productRepository;

    public ProductPdpService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void setAttributes(Long productId, ProductAttributes attributes) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.PRODUCT_NOT_FOUND));
        product.setAttributes(attributes);
        productRepository.save(product);
    }
}
