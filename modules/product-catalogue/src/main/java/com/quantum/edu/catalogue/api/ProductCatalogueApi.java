package com.quantum.edu.catalogue.api;

import com.quantum.edu.catalogue.domain.Product;
import com.quantum.edu.catalogue.dto.CategoryResponse;
import com.quantum.edu.catalogue.dto.ProductDetailResponse;
import com.quantum.edu.catalogue.dto.ProductListResponse;
import com.quantum.edu.catalogue.dto.QuizAnswerInput;
import com.quantum.edu.catalogue.dto.QuizValidationResult;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductCatalogueApi {

    boolean existsById(Long productId);

    boolean isPublished(Long productId);

    Optional<Product> getProduct(Long productId);

    List<ProductListResponse> getFeaturedProducts();

    List<CategoryResponse> getActiveCategories();

    Page<ProductListResponse> getPublishedProducts(Long categoryId, int page, int size);

    ProductDetailResponse getPublishedProductBySlug(String slug);

    ProductDetailResponse getPublishedProductById(Long productId);

    /**
     * Get product ID that contains the given content. Returns empty if content not found.
     */
    Optional<Long> getProductIdByContentId(Long contentId);

    /**
     * Validate quiz answers for an assessment content. Returns validation result with pass/fail and per-question results.
     */
    Optional<QuizValidationResult> validateQuizAnswers(Long productContentId, List<QuizAnswerInput> answers);
}
