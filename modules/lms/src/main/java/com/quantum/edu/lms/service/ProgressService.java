package com.quantum.edu.lms.service;

import com.quantum.edu.catalogue.api.ProductCatalogueApi;
import com.quantum.edu.catalogue.dto.ProductDetailResponse;
import com.quantum.edu.common.exception.ApiErrorCode;
import com.quantum.edu.common.exception.ApiException;
import com.quantum.edu.lms.domain.UserLessonProgress;
import com.quantum.edu.lms.repository.UserLessonProgressRepository;
import com.quantum.edu.ownership.api.OwnershipApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    private final UserLessonProgressRepository progressRepository;
    private final ProductCatalogueApi productCatalogueApi;
    private final OwnershipApi ownershipApi;
    private final QuizService quizService;

    public ProgressService(UserLessonProgressRepository progressRepository,
                          ProductCatalogueApi productCatalogueApi,
                          OwnershipApi ownershipApi,
                          QuizService quizService) {
        this.progressRepository = progressRepository;
        this.productCatalogueApi = productCatalogueApi;
        this.ownershipApi = ownershipApi;
        this.quizService = quizService;
    }

    public Set<Long> getCompletedContentIds(Long userId, Set<Long> contentIds) {
        if (contentIds.isEmpty()) {
            return Set.of();
        }
        List<Long> ids = new ArrayList<>(contentIds);
        return progressRepository.findByUserIdAndProductContentIdIn(userId, ids)
                .stream()
                .map(UserLessonProgress::getProductContentId)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void markComplete(Long userId, Long contentId) {
        Long productId = productCatalogueApi.getProductIdByContentId(contentId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.LMS_CONTENT_NOT_FOUND));

        if (!ownershipApi.ownsCourse(userId, productId)) {
            throw new ApiException(ApiErrorCode.LMS_COURSE_ACCESS_DENIED);
        }

        ProductDetailResponse product = productCatalogueApi.getPublishedProductById(productId);
        ProductDetailResponse.ContentSummary content = product.getContents().stream()
                .filter(c -> c.getId().equals(contentId))
                .findFirst()
                .orElseThrow(() -> new ApiException(ApiErrorCode.LMS_CONTENT_NOT_FOUND));

        if ("ASSESSMENT".equals(content.getContentType())) {
            if (!quizService.hasPassedQuiz(userId, contentId)) {
                throw new ApiException(ApiErrorCode.LMS_QUIZ_NOT_PASSED);
            }
        }

        if (progressRepository.findByUserIdAndProductContentId(userId, contentId).isEmpty()) {
            UserLessonProgress progress = new UserLessonProgress(userId, contentId, Instant.now());
            progressRepository.save(progress);
        }
    }
}
