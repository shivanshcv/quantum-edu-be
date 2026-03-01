package com.quantum.edu.lms.service;

import com.quantum.edu.catalogue.api.ProductCatalogueApi;
import com.quantum.edu.catalogue.dto.ProductDetailResponse;
import com.quantum.edu.common.exception.ApiErrorCode;
import com.quantum.edu.common.exception.ApiException;
import com.quantum.edu.lms.dto.LmsPlayerData;
import com.quantum.edu.ownership.api.OwnershipApi;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LmsService {

    private final ProductCatalogueApi productCatalogueApi;
    private final OwnershipApi ownershipApi;
    private final ProgressService progressService;

    public LmsService(ProductCatalogueApi productCatalogueApi,
                      OwnershipApi ownershipApi,
                      ProgressService progressService) {
        this.productCatalogueApi = productCatalogueApi;
        this.ownershipApi = ownershipApi;
        this.progressService = progressService;
    }

    public LmsPlayerData getPlayerData(Long userId, Long productId) {
        ProductDetailResponse product = productCatalogueApi.getPublishedProductById(productId);

        if (!ownershipApi.ownsCourse(userId, productId)) {
            throw new ApiException(ApiErrorCode.LMS_COURSE_ACCESS_DENIED);
        }

        Set<Long> allContentIds = product.getContents().stream()
                .map(ProductDetailResponse.ContentSummary::getId)
                .collect(Collectors.toSet());
        Set<Long> completedContentIds = progressService.getCompletedContentIds(userId, allContentIds);

        List<LmsPlayerData.ModuleData> modules = buildModules(product, completedContentIds);

        List<LmsPlayerData.LessonData> allLessons = modules.stream()
                .flatMap(m -> m.getLessons().stream())
                .toList();

        int totalLessons = allLessons.size();
        int completedLessons = (int) allLessons.stream()
                .filter(l -> "completed".equals(l.getStatus()))
                .count();
        int progressPercentage = totalLessons > 0 ? (completedLessons * 100) / totalLessons : 0;

        String instructor = "";
        if (product.getAttributes() != null && product.getAttributes().getInstructor() != null) {
            instructor = product.getAttributes().getInstructor().getName() != null
                    ? product.getAttributes().getInstructor().getName() : "";
        }

        return LmsPlayerData.builder()
                .courseSlug(product.getSlug())
                .courseTitle(product.getTitle())
                .subtitle(product.getShortDescription())
                .instructor(instructor)
                .completedLessons(completedLessons)
                .totalLessons(totalLessons)
                .progressPercentage(progressPercentage)
                .modules(modules)
                .build();
    }

    private List<LmsPlayerData.ModuleData> buildModules(ProductDetailResponse product, Set<Long> completedContentIds) {
        List<ProductDetailResponse.ModuleSummary> productModules = product.getModules() != null
                ? product.getModules().stream()
                .sorted(Comparator.comparingInt(ProductDetailResponse.ModuleSummary::getOrderIndex))
                .toList()
                : List.of();

        List<ProductDetailResponse.ContentSummary> orphanContents = product.getContents().stream()
                .filter(c -> c.getModuleId() == null)
                .sorted(Comparator.comparingInt(ProductDetailResponse.ContentSummary::getOrderIndex))
                .toList();

        List<LmsPlayerData.ModuleData> result = new ArrayList<>();

        if (!orphanContents.isEmpty()) {
            long syntheticModuleId = -1L;
            String syntheticTitle = productModules.isEmpty() ? "Course Content" : "Introduction";
            List<LmsPlayerData.LessonData> introLessons = buildLessonsForContent(orphanContents, completedContentIds, false);
            result.add(LmsPlayerData.ModuleData.builder()
                    .id(syntheticModuleId)
                    .title(syntheticTitle)
                    .locked(false)
                    .lessons(introLessons)
                    .build());
        }

        boolean previousModuleComplete = orphanContents.isEmpty() || allLessonsComplete(orphanContents, completedContentIds);

        for (ProductDetailResponse.ModuleSummary mod : productModules) {
            List<ProductDetailResponse.ContentSummary> modContents = mod.getContents() != null
                    ? mod.getContents().stream()
                    .sorted(Comparator.comparingInt(ProductDetailResponse.ContentSummary::getOrderIndex))
                    .toList()
                    : List.of();

            boolean moduleLocked = !previousModuleComplete;
            List<LmsPlayerData.LessonData> lessons = buildLessonsForContent(modContents, completedContentIds, moduleLocked);

            previousModuleComplete = modContents.stream().allMatch(c -> completedContentIds.contains(c.getId()));

            result.add(LmsPlayerData.ModuleData.builder()
                    .id(mod.getId())
                    .title(mod.getTitle())
                    .locked(moduleLocked)
                    .lessons(lessons)
                    .build());
        }

        assignInProgressStatus(result, completedContentIds);

        return result;
    }

    private boolean allLessonsComplete(List<ProductDetailResponse.ContentSummary> contents, Set<Long> completedIds) {
        return contents.stream().allMatch(c -> completedIds.contains(c.getId()));
    }

    private List<LmsPlayerData.LessonData> buildLessonsForContent(
            List<ProductDetailResponse.ContentSummary> contents,
            Set<Long> completedContentIds,
            boolean moduleLocked) {
        List<LmsPlayerData.LessonData> lessons = new ArrayList<>();
        for (ProductDetailResponse.ContentSummary c : contents) {
            String status;
            if (moduleLocked) {
                status = "locked";
            } else if (completedContentIds.contains(c.getId())) {
                status = "completed";
            } else {
                status = "not_started";
            }

            int durationMinutes = c.getDurationSeconds() != null ? (c.getDurationSeconds() + 59) / 60 : 0;
            String videoUrl = "LESSON".equals(c.getContentType()) ? c.getVideoUrl() : null;
            String pdfUrl = "LESSON".equals(c.getContentType()) ? c.getPdfUrl() : null;
            String moduleType = "LESSON".equals(c.getContentType()) ? "LESSON" : "QUIZ";
            String type = "LESSON".equals(c.getContentType()) ? c.getLessonType() : null;
            LmsPlayerData.AssessmentData assessmentData = "ASSESSMENT".equals(c.getContentType()) && c.getAssessment() != null
                    ? toAssessmentData(c.getAssessment())
                    : null;

            lessons.add(LmsPlayerData.LessonData.builder()
                    .contentId(c.getId())
                    .title(c.getTitle())
                    .description(c.getTitle())
                    .durationMinutes(durationMinutes)
                    .status(status)
                    .videoUrl(videoUrl)
                    .pdfUrl(pdfUrl)
                    .moduleType(moduleType)
                    .type(type)
                    .assessment(assessmentData)
                    .build());
        }
        return lessons;
    }

    private LmsPlayerData.AssessmentData toAssessmentData(ProductDetailResponse.AssessmentSummary a) {
        List<LmsPlayerData.QuestionData> questions = a.getQuestions().stream()
                .map(q -> LmsPlayerData.QuestionData.builder()
                        .id(q.getId())
                        .questionText(q.getQuestionText())
                        .options(q.getOptions().stream()
                                .map(o -> LmsPlayerData.OptionData.builder()
                                        .id(o.getId())
                                        .optionText(o.getOptionText())
                                        .build())
                                .toList())
                        .build())
                .toList();
        return LmsPlayerData.AssessmentData.builder()
                .passPercentage(a.getPassPercentage())
                .questions(questions)
                .build();
    }

    private void assignInProgressStatus(List<LmsPlayerData.ModuleData> modules, Set<Long> completedContentIds) {
        boolean foundInProgress = false;
        for (LmsPlayerData.ModuleData mod : modules) {
            for (LmsPlayerData.LessonData lesson : mod.getLessons()) {
                if ("locked".equals(lesson.getStatus())) {
                    continue;
                }
                if (completedContentIds.contains(lesson.getContentId())) {
                    continue;
                }
                if (!foundInProgress) {
                    lesson.setStatus("in_progress");
                    foundInProgress = true;
                }
            }
        }
    }
}
