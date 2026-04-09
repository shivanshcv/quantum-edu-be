package com.quantum.edu.lms;

import com.quantum.edu.catalogue.dto.ProductDetailResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Linear progression rules for LMS: module-level gating (previous segment must be fully complete)
 * and within-module sequential gating (content N+1 locked until content N is complete).
 * Must stay aligned with {@link com.quantum.edu.lms.service.LmsService} player construction.
 */
public final class LmsCourseProgression {

    private LmsCourseProgression() {
    }

    /**
     * @return true if the user must not access this content yet (module or in-module order).
     */
    public static boolean isContentLocked(ProductDetailResponse product, Set<Long> completedContentIds, Long contentId) {
        if (product.getContents() == null
                || product.getContents().stream().noneMatch(c -> c.getId().equals(contentId))) {
            return false;
        }

        List<ProductDetailResponse.ModuleSummary> productModules = product.getModules() != null
                ? product.getModules().stream()
                .sorted(Comparator.comparingInt(ProductDetailResponse.ModuleSummary::getOrderIndex))
                .toList()
                : List.of();

        List<ProductDetailResponse.ContentSummary> orphanContents = product.getContents().stream()
                .filter(c -> c.getModuleId() == null)
                .sorted(Comparator.comparingInt(ProductDetailResponse.ContentSummary::getOrderIndex))
                .toList();

        if (orphanContents.stream().anyMatch(c -> c.getId().equals(contentId))) {
            return isSequentiallyLocked(orphanContents, contentId, completedContentIds);
        }

        boolean previousModuleComplete = orphanContents.isEmpty()
                || allComplete(orphanContents, completedContentIds);

        for (ProductDetailResponse.ModuleSummary mod : productModules) {
            List<ProductDetailResponse.ContentSummary> modContents = orderedModuleContents(product, mod);
            boolean moduleLocked = !previousModuleComplete;
            if (modContents.stream().anyMatch(c -> c.getId().equals(contentId))) {
                if (moduleLocked) {
                    return true;
                }
                return isSequentiallyLocked(modContents, contentId, completedContentIds);
            }
            previousModuleComplete = modContents.stream()
                    .allMatch(c -> completedContentIds.contains(c.getId()));
        }

        // moduleId set but not present in any module's content list — treat as inaccessible.
        return true;
    }

    private static List<ProductDetailResponse.ContentSummary> orderedModuleContents(
            ProductDetailResponse product, ProductDetailResponse.ModuleSummary mod) {
        if (mod.getContents() != null && !mod.getContents().isEmpty()) {
            return mod.getContents().stream()
                    .sorted(Comparator.comparingInt(ProductDetailResponse.ContentSummary::getOrderIndex))
                    .toList();
        }
        return product.getContents().stream()
                .filter(c -> mod.getId().equals(c.getModuleId()))
                .sorted(Comparator.comparingInt(ProductDetailResponse.ContentSummary::getOrderIndex))
                .toList();
    }

    private static boolean allComplete(List<ProductDetailResponse.ContentSummary> contents, Set<Long> completed) {
        return contents.stream().allMatch(c -> completed.contains(c.getId()));
    }

    private static boolean isSequentiallyLocked(
            List<ProductDetailResponse.ContentSummary> orderedContents,
            Long contentId,
            Set<Long> completed) {
        for (int i = 0; i < orderedContents.size(); i++) {
            if (!orderedContents.get(i).getId().equals(contentId)) {
                continue;
            }
            for (int j = 0; j < i; j++) {
                if (!completed.contains(orderedContents.get(j).getId())) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
