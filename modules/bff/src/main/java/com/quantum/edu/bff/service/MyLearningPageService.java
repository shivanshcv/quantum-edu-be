package com.quantum.edu.bff.service;

import com.quantum.edu.bff.config.MyLearningProperties;
import com.quantum.edu.bff.dto.*;
import com.quantum.edu.catalogue.api.ProductCatalogueApi;
import com.quantum.edu.catalogue.dto.CategoryResponse;
import com.quantum.edu.catalogue.dto.ProductDetailResponse;
import com.quantum.edu.ownership.api.OwnershipApi;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MyLearningPageService {

    private final MyLearningProperties props;
    private final OwnershipApi ownershipApi;
    private final ProductCatalogueApi productCatalogueApi;

    public MyLearningPageService(MyLearningProperties props,
                                 OwnershipApi ownershipApi,
                                 ProductCatalogueApi productCatalogueApi) {
        this.props = props;
        this.ownershipApi = ownershipApi;
        this.productCatalogueApi = productCatalogueApi;
    }

    public MyLearningPageResponse getMyLearningPage(Long userId) {
        List<Long> courseIds = ownershipApi.getEnrolledCourseIds(userId);
        int enrollmentCount = courseIds.size();

        List<MyLearningPageResponse.MyLearningSection> sections = new ArrayList<>();
        for (Long productId : courseIds) {
            try {
                ProductDetailResponse product = productCatalogueApi.getPublishedProductById(productId);
                if (product != null) {
                    sections.add(toSection(product));
                }
            } catch (Exception ignored) {
                // Skip if product not found or unpublished
            }
        }

        var emptyStateProps = props.getEmptyState();
        var ctaProps = emptyStateProps != null && emptyStateProps.getCta() != null ? emptyStateProps.getCta() : null;
        MyLearningPageResponse.EmptyStateResponse emptyState = MyLearningPageResponse.EmptyStateResponse.builder()
                .icon(emptyStateProps != null ? emptyStateProps.getIcon() : "book")
                .title(emptyStateProps != null ? emptyStateProps.getTitle() : "No active enrollments")
                .message(emptyStateProps != null ? emptyStateProps.getMessage() : "Your learning path is empty. Explore our catalog to find the program that fits your career goals.")
                .cta(ctaProps != null ? CtaResponse.builder()
                        .label(ctaProps.getLabel())
                        .url(ctaProps.getUrl())
                        .variant(ctaProps.getVariant())
                        .type(ctaProps.getType())
                        .build() : CtaResponse.builder().label("FIND A PROGRAM").url("/courses").variant("primary").type("button").build())
                .build();

        return MyLearningPageResponse.builder()
                .badge(props.getBadge())
                .title(props.getTitle())
                .subtitle(props.getSubtitle())
                .enrollmentCount(enrollmentCount)
                .emptyState(emptyState)
                .sections(sections)
                .build();
    }

    private MyLearningPageResponse.MyLearningSection toSection(ProductDetailResponse product) {
        String rootCategoryName = getRootCategoryName(product);
        ImageResponse image = product.getThumbnailUrl() != null
                ? ImageResponse.builder().src(product.getThumbnailUrl()).alt(product.getTitle()).build()
                : null;

        CtaResponse continueCta = CtaResponse.builder()
                .label("CONTINUE")
                .url("/lms/" + product.getId())
                .variant("primary")
                .type("button")
                .build();

        return MyLearningPageResponse.MyLearningSection.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getShortDescription())
                .badge(rootCategoryName != null ? rootCategoryName : "Course")
                .image(image)
                .ctas(List.of(continueCta))
                .build();
    }

    private String getRootCategoryName(ProductDetailResponse product) {
        if (product == null || product.getCategories() == null || product.getCategories().isEmpty()) {
            return null;
        }
        return product.getCategories().stream()
                .filter(c -> c.getLevel() == 0)
                .map(CategoryResponse::getName)
                .findFirst()
                .orElse(product.getCategories().get(0).getName());
    }
}
