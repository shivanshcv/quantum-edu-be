package com.quantum.edu.bff.service;

import com.quantum.edu.bff.dto.*;
import com.quantum.edu.catalogue.api.ProductCatalogueApi;
import com.quantum.edu.catalogue.domain.ProductAttributes;
import com.quantum.edu.catalogue.dto.ProductDetailResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PDPPageService {

    private final ProductCatalogueApi productCatalogueApi;

    public PDPPageService(ProductCatalogueApi productCatalogueApi) {
        this.productCatalogueApi = productCatalogueApi;
    }

    public PageResponse getProductDetailPage(Long productId) {
        ProductDetailResponse product = productCatalogueApi.getPublishedProductById(productId);
        ProductAttributes attrs = product.getAttributes();

        List<ComponentResponse> components = new ArrayList<>();
        components.add(buildCourseHeroDetails(product, attrs));
        components.add(buildCourseLearningOutcomes(attrs));
        components.add(buildCourseSyllabus(product));
        components.add(buildInstructorProfile(attrs));
        components.add(buildCourseCertification(attrs));

        MainSection main = MainSection.builder()
                .type("COURSE_DETAILS")
                .components(components)
                .data(Map.of("courseId", product.getId(), "slug", product.getSlug()))
                .build();

        return PageResponse.builder().main(main).build();
    }

    private ComponentResponse buildCourseHeroDetails(ProductDetailResponse product, ProductAttributes attrs) {
        List<HighlightResponse> highlights = List.of();
        if (attrs != null && attrs.getHighlights() != null) {
            highlights = attrs.getHighlights().stream()
                    .map(h -> HighlightResponse.builder()
                            .icon(h.getIcon())
                            .label(h.getLabel())
                            .value(h.getValue())
                            .build())
                    .toList();
        }

        String priceStr = product.getDiscountPrice() != null
                ? "₹" + product.getDiscountPrice().toPlainString()
                : "₹" + product.getPrice().toPlainString();

        String badge = (attrs != null && attrs.getBadge() != null) ? attrs.getBadge() : null;

        CourseHeroDetails details = CourseHeroDetails.builder()
                .badge(badge)
                .title(product.getTitle())
                .shortDescription(product.getShortDescription())
                .image(ImageResponse.builder()
                        .src(product.getThumbnailUrl())
                        .alt(product.getTitle())
                        .build())
                .highlights(highlights)
                .priceDetails(PriceDetailsResponse.builder().price(priceStr).build())
                .ctas(List.of(
                        CtaResponse.builder()
                                .label("Enroll Now")
                                .action("ENROLL")
                                .variant("primary")
                                .build()
                ))
                .build();

        return ComponentResponse.builder()
                .type("COURSE_HERO_DETAILS")
                .details(details)
                .build();
    }

    private ComponentResponse buildCourseLearningOutcomes(ProductAttributes attrs) {
        List<LearningOutcomeResponse> outcomes = List.of();
        if (attrs != null && attrs.getLearningOutcomes() != null) {
            List<String> rawOutcomes = attrs.getLearningOutcomes();
            outcomes = new ArrayList<>();
            for (int i = 0; i < rawOutcomes.size(); i++) {
                outcomes.add(LearningOutcomeResponse.builder()
                        .id((long) (i + 1))
                        .text(rawOutcomes.get(i))
                        .checked(true)
                        .build());
            }
        }

        CourseLearningOutcomesDetails details = CourseLearningOutcomesDetails.builder()
                .title("What You'll Learn")
                .outcomes(outcomes)
                .build();

        return ComponentResponse.builder()
                .type("COURSE_LEARNING_OUTCOMES")
                .details(details)
                .build();
    }

    private ComponentResponse buildCourseSyllabus(ProductDetailResponse product) {
        List<SyllabusModuleResponse> modules = List.of();
        if (product.getModules() != null) {
            modules = product.getModules().stream()
                    .map(this::toSyllabusModule)
                    .toList();
        }

        CourseSyllabusDetails details = CourseSyllabusDetails.builder()
                .title("Course Curriculum")
                .modules(modules)
                .build();

        return ComponentResponse.builder()
                .type("COURSE_SYLLABUS")
                .details(details)
                .build();
    }

    private SyllabusModuleResponse toSyllabusModule(ProductDetailResponse.ModuleSummary module) {
        List<SyllabusLessonResponse> lessons = List.of();
        if (module.getContents() != null) {
            lessons = module.getContents().stream()
                    .map(c -> SyllabusLessonResponse.builder()
                            .id(c.getId())
                            .title(c.getTitle())
                            .build())
                    .toList();
        }

        String duration = computeModuleDuration(module);

        return SyllabusModuleResponse.builder()
                .id(module.getId())
                .title(module.getTitle())
                .duration(duration)
                .lessons(lessons)
                .build();
    }

    private String computeModuleDuration(ProductDetailResponse.ModuleSummary module) {
        if (module.getContents() == null || module.getContents().isEmpty()) {
            return null;
        }

        int totalSeconds = module.getContents().stream()
                .filter(c -> c.getDurationSeconds() != null)
                .mapToInt(ProductDetailResponse.ContentSummary::getDurationSeconds)
                .sum();

        if (totalSeconds == 0) {
            return module.getContents().size() + " lessons";
        }

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;

        if (hours > 0 && minutes > 0) {
            return hours + "h " + minutes + "m";
        } else if (hours > 0) {
            return hours + "h";
        } else {
            return minutes + "m";
        }
    }

    private ComponentResponse buildInstructorProfile(ProductAttributes attrs) {
        InstructorDetailResponse instructorDetail = null;
        if (attrs != null && attrs.getInstructor() != null) {
            ProductAttributes.InstructorInfo info = attrs.getInstructor();
            instructorDetail = InstructorDetailResponse.builder()
                    .name(info.getName())
                    .role(info.getRole())
                    .image(ImageResponse.builder()
                            .src(info.getImageUrl())
                            .alt(info.getName())
                            .build())
                    .bio(info.getBio())
                    .credentials(info.getCredentials())
                    .build();
        }

        InstructorProfileDetails details = InstructorProfileDetails.builder()
                .title("Meet Your Instructor")
                .instructor(instructorDetail)
                .build();

        return ComponentResponse.builder()
                .type("INSTRUCTOR_PROFILE")
                .details(details)
                .build();
    }

    private ComponentResponse buildCourseCertification(ProductAttributes attrs) {
        CertificationDetailResponse certDetail = null;
        if (attrs != null && attrs.getCertification() != null) {
            ProductAttributes.CertificationInfo info = attrs.getCertification();
            certDetail = CertificationDetailResponse.builder()
                    .icon(info.getIcon())
                    .title(info.getTitle())
                    .description(info.getDescription())
                    .highlights(info.getHighlights())
                    .build();
        }

        List<OutcomeHighlightResponse> outcomeHighlights = List.of();
        if (attrs != null && attrs.getOutcomeHighlights() != null) {
            List<ProductAttributes.OutcomeHighlight> raw = attrs.getOutcomeHighlights();
            outcomeHighlights = new ArrayList<>();
            for (int i = 0; i < raw.size(); i++) {
                outcomeHighlights.add(OutcomeHighlightResponse.builder()
                        .id((long) (i + 1))
                        .title(raw.get(i).getTitle())
                        .description(raw.get(i).getDescription())
                        .build());
            }
        }

        CourseCertificationDetails details = CourseCertificationDetails.builder()
                .title("Certification & Career Outcomes")
                .certificationDetails(certDetail)
                .outcomesHighlights(outcomeHighlights)
                .build();

        return ComponentResponse.builder()
                .type("COURSE_CERTIFICATION")
                .details(details)
                .build();
    }
}
