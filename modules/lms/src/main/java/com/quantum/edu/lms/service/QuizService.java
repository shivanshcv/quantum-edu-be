package com.quantum.edu.lms.service;

import com.quantum.edu.catalogue.api.ProductCatalogueApi;
import com.quantum.edu.catalogue.dto.ProductDetailResponse;
import com.quantum.edu.catalogue.dto.QuizAnswerInput;
import com.quantum.edu.catalogue.dto.QuizValidationResult;
import com.quantum.edu.common.exception.ApiErrorCode;
import com.quantum.edu.common.exception.ApiException;
import com.quantum.edu.lms.LmsCourseProgression;
import com.quantum.edu.lms.domain.UserAssessmentResult;
import com.quantum.edu.lms.dto.QuizValidationResponse;
import com.quantum.edu.lms.domain.UserLessonProgress;
import com.quantum.edu.lms.repository.UserAssessmentResultRepository;
import com.quantum.edu.lms.repository.UserLessonProgressRepository;
import com.quantum.edu.ownership.api.OwnershipApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final ProductCatalogueApi productCatalogueApi;
    private final OwnershipApi ownershipApi;
    private final UserAssessmentResultRepository assessmentResultRepository;
    private final UserLessonProgressRepository lessonProgressRepository;

    public QuizService(ProductCatalogueApi productCatalogueApi,
                      OwnershipApi ownershipApi,
                      UserAssessmentResultRepository assessmentResultRepository,
                      UserLessonProgressRepository lessonProgressRepository) {
        this.productCatalogueApi = productCatalogueApi;
        this.ownershipApi = ownershipApi;
        this.assessmentResultRepository = assessmentResultRepository;
        this.lessonProgressRepository = lessonProgressRepository;
    }

    @Transactional
    public QuizValidationResponse validateAndStore(Long userId, Long contentId, List<QuizAnswerInput> answers) {
        Long productId = productCatalogueApi.getProductIdByContentId(contentId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.LMS_CONTENT_NOT_FOUND));

        if (!ownershipApi.ownsCourse(userId, productId)) {
            throw new ApiException(ApiErrorCode.LMS_COURSE_ACCESS_DENIED);
        }

        ProductDetailResponse product = productCatalogueApi.getPublishedProductById(productId);
        boolean isAssessment = product.getContents().stream()
                .anyMatch(c -> c.getId().equals(contentId) && "ASSESSMENT".equals(c.getContentType()));
        if (!isAssessment) {
            throw new ApiException(ApiErrorCode.LMS_CONTENT_NOT_FOUND);
        }

        List<Long> allContentIdList = new ArrayList<>(product.getContents().stream()
                .map(ProductDetailResponse.ContentSummary::getId)
                .toList());
        Set<Long> completedContentIds = allContentIdList.isEmpty()
                ? Set.of()
                : lessonProgressRepository.findByUserIdAndProductContentIdIn(userId, allContentIdList).stream()
                        .map(UserLessonProgress::getProductContentId)
                        .collect(Collectors.toSet());
        if (LmsCourseProgression.isContentLocked(product, completedContentIds, contentId)) {
            throw new ApiException(ApiErrorCode.LMS_CONTENT_LOCKED);
        }

        QuizValidationResult result = productCatalogueApi.validateQuizAnswers(contentId, answers)
                .orElseThrow(() -> new ApiException(ApiErrorCode.LMS_CONTENT_NOT_FOUND));

        if (result.isPassed()) {
            Optional<UserAssessmentResult> existing = assessmentResultRepository.findByUserIdAndProductContentId(userId, contentId);
            if (existing.isPresent()) {
                UserAssessmentResult r = existing.get();
                r.setPassed(true);
                r.setScorePercentage(result.getScorePercentage());
                assessmentResultRepository.save(r);
            } else {
                assessmentResultRepository.save(new UserAssessmentResult(userId, contentId, true, result.getScorePercentage()));
            }
        }

        return toResponse(result);
    }

    public boolean hasPassedQuiz(Long userId, Long contentId) {
        return assessmentResultRepository.findByUserIdAndProductContentId(userId, contentId)
                .map(UserAssessmentResult::isPassed)
                .orElse(false);
    }

    private QuizValidationResponse toResponse(QuizValidationResult r) {
        List<QuizValidationResponse.QuestionResult> results = r.getResults().stream()
                .map(qr -> QuizValidationResponse.QuestionResult.builder()
                        .questionId(qr.getQuestionId())
                        .correct(qr.isCorrect())
                        .build())
                .toList();
        return QuizValidationResponse.builder()
                .passed(r.isPassed())
                .scorePercentage(r.getScorePercentage())
                .passPercentage(r.getPassPercentage())
                .results(results)
                .build();
    }
}
