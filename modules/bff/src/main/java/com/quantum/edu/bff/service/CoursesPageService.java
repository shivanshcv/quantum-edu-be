package com.quantum.edu.bff.service;

import com.quantum.edu.bff.config.CourseCatalogProperties;
import com.quantum.edu.bff.dto.*;
import com.quantum.edu.catalogue.api.ProductCatalogueApi;
import com.quantum.edu.catalogue.dto.CategoryResponse;
import com.quantum.edu.catalogue.dto.ProductListResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CoursesPageService {

    private final CourseCatalogProperties catalogProps;
    private final ProductCatalogueApi productCatalogueApi;

    public CoursesPageService(CourseCatalogProperties catalogProps, ProductCatalogueApi productCatalogueApi) {
        this.catalogProps = catalogProps;
        this.productCatalogueApi = productCatalogueApi;
    }

    public PageResponse getCoursesPage(Long categoryId, int page, int size) {
        List<FilterResponse> filters = buildFilters();
        Page<ProductListResponse> productPage = productCatalogueApi.getPublishedProducts(categoryId, page, size);

        List<CardSection> sections = productPage.getContent().stream()
                .map(this::toCardSection)
                .toList();

        PaginationResponse pagination = PaginationResponse.builder()
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .hasNext(productPage.hasNext())
                .build();

        CourseCatalogDetails details = CourseCatalogDetails.builder()
                .badge(catalogProps.getBadge())
                .title(catalogProps.getTitle())
                .subtitle(catalogProps.getSubtitle())
                .filters(filters)
                .sections(sections)
                .pagination(pagination)
                .build();

        ComponentResponse catalogComponent = ComponentResponse.builder()
                .type("COURSE_CATALOG")
                .config(Map.of("theme", "brand-light-green", "layout", "grid-3-col"))
                .details(details)
                .build();

        MainSection main = MainSection.builder()
                .type("CATALOG")
                .components(List.of(catalogComponent))
                .data(Map.of())
                .build();

        return PageResponse.builder().main(main).build();
    }

    private List<FilterResponse> buildFilters() {
        List<CategoryResponse> activeCategories = productCatalogueApi.getActiveCategories();

        List<FilterResponse> filters = new ArrayList<>();
        filters.add(FilterResponse.builder()
                .id(null)
                .value("all")
                .label("All")
                .build());

        activeCategories.forEach(cat -> filters.add(FilterResponse.builder()
                .id(cat.getId())
                .value(cat.getSlug())
                .label(cat.getName())
                .build()));

        return filters;
    }

    private CardSection toCardSection(ProductListResponse product) {
        String badge = product.getCategories() != null && !product.getCategories().isEmpty()
                ? product.getCategories().get(0).getSlug()
                : null;

        BigDecimal displayPrice = product.getDiscountPrice() != null
                && product.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0
                ? product.getDiscountPrice()
                : product.getPrice();

        return CardSection.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getShortDescription())
                .badge(badge)
                .image(ImageResponse.builder()
                        .src(product.getThumbnailUrl())
                        .alt(product.getTitle())
                        .build())
                .priceDetails(PriceDetailsResponse.builder()
                        .price(formatPrice(displayPrice))
                        .build())
                .ctas(List.of(CtaResponse.builder()
                        .label("Enroll Now")
                        .url("/course/" + product.getId())
                        .variant("primary")
                        .type("button")
                        .action("native")
                        .build()))
                .build();
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "$0.00";
        NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.US);
        return fmt.format(price);
    }
}
