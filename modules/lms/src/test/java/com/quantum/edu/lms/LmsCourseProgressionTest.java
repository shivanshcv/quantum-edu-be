package com.quantum.edu.lms;

import com.quantum.edu.catalogue.dto.ProductDetailResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LmsCourseProgressionTest {

    @Test
    void introTwoContents_secondLockedUntilFirstComplete() {
        ProductDetailResponse product = ProductDetailResponse.builder()
                .contents(List.of(
                        content(1L, null, 0),
                        content(2L, null, 1)
                ))
                .modules(List.of())
                .build();

        assertFalse(LmsCourseProgression.isContentLocked(product, Set.of(), 1L));
        assertTrue(LmsCourseProgression.isContentLocked(product, Set.of(), 2L));

        assertFalse(LmsCourseProgression.isContentLocked(product, Set.of(1L), 2L));
    }

    @Test
    void secondModuleLockedUntilFirstModuleFullyComplete() {
        ProductDetailResponse.ModuleSummary mod1 = ProductDetailResponse.ModuleSummary.builder()
                .id(10L)
                .title("M1")
                .orderIndex(0)
                .contents(List.of(content(1L, 10L, 0), content(2L, 10L, 1)))
                .build();
        ProductDetailResponse.ModuleSummary mod2 = ProductDetailResponse.ModuleSummary.builder()
                .id(11L)
                .title("M2")
                .orderIndex(1)
                .contents(List.of(content(3L, 11L, 0)))
                .build();

        ProductDetailResponse product = ProductDetailResponse.builder()
                .contents(List.of(
                        content(1L, 10L, 0),
                        content(2L, 10L, 1),
                        content(3L, 11L, 0)
                ))
                .modules(List.of(mod1, mod2))
                .build();

        assertFalse(LmsCourseProgression.isContentLocked(product, Set.of(), 1L));
        assertTrue(LmsCourseProgression.isContentLocked(product, Set.of(), 2L));
        assertTrue(LmsCourseProgression.isContentLocked(product, Set.of(1L), 3L));

        assertFalse(LmsCourseProgression.isContentLocked(product, Set.of(1L, 2L), 3L));
    }

    @Test
    void completedLessonsRemainReachable() {
        ProductDetailResponse product = ProductDetailResponse.builder()
                .contents(List.of(content(1L, null, 0), content(2L, null, 1)))
                .modules(List.of())
                .build();

        assertFalse(LmsCourseProgression.isContentLocked(product, Set.of(1L, 2L), 1L));
    }

    private static ProductDetailResponse.ContentSummary content(long id, Long moduleId, int orderIndex) {
        ProductDetailResponse.ContentSummary c = new ProductDetailResponse.ContentSummary();
        c.setId(id);
        c.setModuleId(moduleId);
        c.setOrderIndex(orderIndex);
        c.setContentType("LESSON");
        c.setTitle("L");
        c.setMandatory(true);
        return c;
    }
}
