package com.quantum.edu.catalogue.service;

import com.quantum.edu.catalogue.domain.Assessment;
import com.quantum.edu.catalogue.domain.Category;
import com.quantum.edu.catalogue.domain.Lesson;
import com.quantum.edu.catalogue.domain.Product;
import com.quantum.edu.catalogue.domain.ProductContent;
import com.quantum.edu.catalogue.dto.*;
import com.quantum.edu.catalogue.repository.AssessmentRepository;
import com.quantum.edu.catalogue.repository.CategoryRepository;
import com.quantum.edu.catalogue.repository.LessonRepository;
import com.quantum.edu.catalogue.repository.ProductRepository;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final LessonRepository lessonRepository;
    private final AssessmentRepository assessmentRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          LessonRepository lessonRepository,
                          AssessmentRepository assessmentRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.lessonRepository = lessonRepository;
        this.assessmentRepository = assessmentRepository;
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
        product.setFree(request.isFree());

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
        if (request.getFree() != null) product.setFree(request.getFree());

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
        Product product = productRepository.findByIdAndPublishedTrueWithContents(id)
                .orElseThrow(() -> new InternalException(InternalErrorCode.PRODUCT_NOT_FOUND));
        return toDetailResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new InternalException(InternalErrorCode.PRODUCT_NOT_FOUND));
        return toDetailResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductListResponse> getFeaturedProducts() {
        return productRepository.findByPublishedTrueAndFeaturedTrue()
                .stream()
                .map(this::toListResponse)
                .toList();
    }

    @Transactional
    public ProductListResponse setFeatured(Long id, boolean featured) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new InternalException(InternalErrorCode.PRODUCT_NOT_FOUND));
        product.setFeatured(featured);
        product = productRepository.save(product);
        return toListResponse(product);
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

        List<ProductDetailResponse.ModuleSummary> modules = product.getModules().stream()
                .map(m -> ProductDetailResponse.ModuleSummary.builder()
                        .id(m.getId())
                        .title(m.getTitle())
                        .orderIndex(m.getOrderIndex())
                        .contents(product.getContents().stream()
                                .filter(c -> c.getModule() != null && c.getModule().getId().equals(m.getId()))
                                .sorted(Comparator.comparingInt(ProductContent::getOrderIndex))
                                .map(this::toContentSummary)
                                .toList())
                        .build())
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
                .free(product.isFree())
                .attributes(product.getAttributes())
                .categories(categories)
                .contents(contents)
                .modules(modules)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductDetailResponse.ContentSummary toContentSummary(ProductContent content) {
        Integer durationSeconds = null;
        String url = null;
        String lessonType = null;
        ProductDetailResponse.AssessmentSummary assessmentSummary = null;

        if (content.getContentType() == ProductContent.ContentType.LESSON) {
            Lesson lesson = lessonRepository.findByProductContentId(content.getId()).orElse(null);
            if (lesson != null) {
                durationSeconds = lesson.getDurationSeconds();
                lessonType = lesson.getLessonType().name();
                url = getMediaUrlForLessonType(lesson);
            }
        } else if (content.getContentType() == ProductContent.ContentType.ASSESSMENT) {
            assessmentSummary = assessmentRepository.findByProductContentIdWithQuestionsAndOptions(content.getId())
                    .map(this::toAssessmentSummary)
                    .orElse(null);
        }

        return ProductDetailResponse.ContentSummary.builder()
                .id(content.getId())
                .contentType(content.getContentType().name())
                .title(content.getTitle())
                .orderIndex(content.getOrderIndex())
                .mandatory(content.isMandatory())
                .moduleId(content.getModule() != null ? content.getModule().getId() : null)
                .durationSeconds(durationSeconds)
                .url(url)
                .lessonType(lessonType)
                .assessment(assessmentSummary)
                .build();
    }

    private String getMediaUrlForLessonType(Lesson lesson) {
        return switch (lesson.getLessonType()) {
            case VIDEO -> lesson.getVideoUrl();
            case PDF -> lesson.getPdfUrl();
            case PPT -> lesson.getPptUrl();
        };
    }

    private ProductDetailResponse.AssessmentSummary toAssessmentSummary(Assessment assessment) {
        List<ProductDetailResponse.QuestionSummary> questions = assessment.getQuestions().stream()
                .map(q -> ProductDetailResponse.QuestionSummary.builder()
                        .id(q.getId())
                        .questionText(q.getQuestionText())
                        .options(q.getOptions().stream()
                                .map(o -> ProductDetailResponse.OptionSummary.builder()
                                        .id(o.getId())
                                        .optionText(o.getOptionText())
                                        .build())
                                .toList())
                        .build())
                .toList();

        return ProductDetailResponse.AssessmentSummary.builder()
                .passPercentage(assessment.getPassPercentage())
                .questions(questions)
                .build();
    }
}
