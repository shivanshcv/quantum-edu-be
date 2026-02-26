package com.quantum.edu.catalogue.service;

import com.quantum.edu.catalogue.domain.Category;
import com.quantum.edu.catalogue.domain.Product;
import com.quantum.edu.catalogue.domain.ProductContent;
import com.quantum.edu.catalogue.dto.*;
import com.quantum.edu.catalogue.repository.CategoryRepository;
import com.quantum.edu.catalogue.repository.ProductRepository;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ProductListResponse create(CreateProductRequest request) {
        String slug = SlugUtil.generate(request.getTitle(), productRepository::existsBySlug);

        Product product = new Product(
                request.getTitle(), slug,
                request.getShortDescription(), request.getLongDescription(),
                request.getPrice(), request.getDifficultyLevel());

        product.setDiscountPrice(request.getDiscountPrice());
        product.setThumbnailUrl(request.getThumbnailUrl());
        product.setPreviewVideoUrl(request.getPreviewVideoUrl());
        product.setDurationMinutes(request.getDurationMinutes());

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
            product.setCategories(categories);
        }

        product = productRepository.save(product);
        return toListResponse(product);
    }

    @Transactional
    public ProductListResponse update(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new InternalException(InternalErrorCode.PRODUCT_NOT_FOUND));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            product.setTitle(request.getTitle());
            String slug = SlugUtil.generate(request.getTitle(), s ->
                    productRepository.findBySlug(s).map(p -> !p.getId().equals(id)).orElse(false));
            product.setSlug(slug);
        }
        if (request.getShortDescription() != null) product.setShortDescription(request.getShortDescription());
        if (request.getLongDescription() != null) product.setLongDescription(request.getLongDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getDiscountPrice() != null) product.setDiscountPrice(request.getDiscountPrice());
        if (request.getThumbnailUrl() != null) product.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getPreviewVideoUrl() != null) product.setPreviewVideoUrl(request.getPreviewVideoUrl());
        if (request.getDifficultyLevel() != null) product.setDifficultyLevel(request.getDifficultyLevel());
        if (request.getDurationMinutes() != null) product.setDurationMinutes(request.getDurationMinutes());

        if (request.getCategoryIds() != null) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
            product.setCategories(categories);
        }

        product = productRepository.save(product);
        return toListResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new InternalException(InternalErrorCode.PRODUCT_NOT_FOUND));
        product.setPublished(false);
        productRepository.save(product);
    }

    @Transactional
    public ProductListResponse setPublished(Long id, boolean published) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new InternalException(InternalErrorCode.PRODUCT_NOT_FOUND));
        product.setPublished(published);
        product = productRepository.save(product);
        return toListResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductListResponse> listAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toListResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductListResponse> searchPublished(String search, Long categoryId,
                                                     Product.DifficultyLevel difficulty, Pageable pageable) {
        if (categoryId != null) {
            return productRepository.findPublishedByCategoryId(categoryId, pageable)
                    .map(this::toListResponse);
        }
        return productRepository.searchPublished(search, difficulty, pageable)
                .map(this::toListResponse);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getPublishedBySlug(String slug) {
        Product product = productRepository.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new InternalException(InternalErrorCode.PRODUCT_NOT_FOUND));
        return toDetailResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getPublishedById(Long id) {
        Product product = productRepository.findByIdAndPublishedTrue(id)
                .orElseThrow(() -> new InternalException(InternalErrorCode.PRODUCT_NOT_FOUND));
        return toDetailResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new InternalException(InternalErrorCode.PRODUCT_NOT_FOUND));
        return toDetailResponse(product);
    }

    private ProductListResponse toListResponse(Product product) {
        List<CategoryResponse> categories = product.getCategories().stream()
                .map(c -> CategoryService.toResponse(c, false))
                .toList();

        return ProductListResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .slug(product.getSlug())
                .shortDescription(product.getShortDescription())
                .longDescription(product.getLongDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .thumbnailUrl(product.getThumbnailUrl())
                .previewVideoUrl(product.getPreviewVideoUrl())
                .difficultyLevel(product.getDifficultyLevel() != null ? product.getDifficultyLevel().name() : null)
                .durationMinutes(product.getDurationMinutes())
                .published(product.isPublished())
                .categories(categories)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductDetailResponse toDetailResponse(Product product) {
        List<CategoryResponse> categories = product.getCategories().stream()
                .map(c -> CategoryService.toResponse(c, false))
                .toList();

        List<ProductDetailResponse.ContentSummary> contents = product.getContents().stream()
                .map(this::toContentSummary)
                .toList();

        return ProductDetailResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .slug(product.getSlug())
                .shortDescription(product.getShortDescription())
                .longDescription(product.getLongDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .thumbnailUrl(product.getThumbnailUrl())
                .previewVideoUrl(product.getPreviewVideoUrl())
                .difficultyLevel(product.getDifficultyLevel() != null ? product.getDifficultyLevel().name() : null)
                .durationMinutes(product.getDurationMinutes())
                .published(product.isPublished())
                .categories(categories)
                .contents(contents)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductDetailResponse.ContentSummary toContentSummary(ProductContent content) {
        return ProductDetailResponse.ContentSummary.builder()
                .id(content.getId())
                .contentType(content.getContentType().name())
                .title(content.getTitle())
                .orderIndex(content.getOrderIndex())
                .mandatory(content.isMandatory())
                .build();
    }
}
