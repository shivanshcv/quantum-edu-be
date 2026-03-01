package com.quantum.edu.catalogue.api;

import com.quantum.edu.catalogue.domain.Product;
import com.quantum.edu.catalogue.dto.CategoryResponse;
import com.quantum.edu.catalogue.dto.ProductDetailResponse;
import com.quantum.edu.catalogue.dto.ProductListResponse;
import com.quantum.edu.catalogue.dto.QuizAnswerInput;
import com.quantum.edu.catalogue.dto.QuizValidationResult;
import com.quantum.edu.catalogue.repository.ProductContentRepository;
import com.quantum.edu.catalogue.repository.ProductRepository;
import com.quantum.edu.catalogue.service.CategoryService;
import com.quantum.edu.catalogue.service.ProductService;
import com.quantum.edu.catalogue.service.QuizValidationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductCatalogueApiImpl implements ProductCatalogueApi {

    private final ProductRepository productRepository;
    private final ProductContentRepository contentRepository;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final QuizValidationService quizValidationService;

    public ProductCatalogueApiImpl(ProductRepository productRepository,
                                   ProductContentRepository contentRepository,
                                   ProductService productService,
                                   CategoryService categoryService,
                                   QuizValidationService quizValidationService) {
        this.productRepository = productRepository;
        this.contentRepository = contentRepository;
        this.productService = productService;
        this.categoryService = categoryService;
        this.quizValidationService = quizValidationService;
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

    @Override
    public List<ProductListResponse> getFeaturedProducts() {
        return productService.getFeaturedProducts();
    }

    @Override
    public List<CategoryResponse> getActiveCategories() {
        return categoryService.listActive();
    }

    @Override
    public Page<ProductListResponse> getPublishedProducts(Long categoryId, int page, int size) {
        return productService.searchPublished(null, categoryId, null, PageRequest.of(page, size));
    }

    @Override
    public ProductDetailResponse getPublishedProductBySlug(String slug) {
        return productService.getPublishedBySlug(slug);
    }

    @Override
    public ProductDetailResponse getPublishedProductById(Long productId) {
        return productService.getPublishedById(productId);
    }

    @Override
    public Optional<Long> getProductIdByContentId(Long contentId) {
        return contentRepository.findProductIdByContentId(contentId);
    }

    @Override
    public Optional<QuizValidationResult> validateQuizAnswers(Long productContentId, List<QuizAnswerInput> answers) {
        return quizValidationService.validateAnswers(productContentId, answers);
    }
}
