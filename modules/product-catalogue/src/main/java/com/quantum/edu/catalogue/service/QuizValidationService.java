package com.quantum.edu.catalogue.service;

import com.quantum.edu.catalogue.domain.Assessment;
import com.quantum.edu.catalogue.domain.AssessmentOption;
import com.quantum.edu.catalogue.domain.AssessmentQuestion;
import com.quantum.edu.catalogue.dto.QuizAnswerInput;
import com.quantum.edu.catalogue.dto.QuizValidationResult;
import com.quantum.edu.catalogue.repository.AssessmentRepository;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuizValidationService {

    private static final Logger log = LoggerFactory.getLogger(QuizValidationService.class);

    private final AssessmentRepository assessmentRepository;

    public QuizValidationService(AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
    }

    @Transactional(readOnly = true)
    public Optional<QuizValidationResult> validateAnswers(Long productContentId, List<QuizAnswerInput> answers) {
        // Same fetch path as player payload (questions eager); options load in-session when scored.
        Assessment assessment = assessmentRepository.findByProductContentIdWithQuestionsAndOptions(productContentId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.ASSESSMENT_NOT_FOUND));

        Map<Long, Long> userAnswers = answers.stream()
                .collect(Collectors.toMap(QuizAnswerInput::getQuestionId, QuizAnswerInput::getOptionId, (a, b) -> b));

        List<QuizValidationResult.QuestionResult> results = assessment.getQuestions().stream()
                .map(q -> {
                    boolean hasCorrectKey = q.getOptions().stream().anyMatch(AssessmentOption::isCorrect);
                    if (!hasCorrectKey) {
                        log.warn("Quiz question id={} has no option with is_correct=1; submissions will always count as wrong. Fix data.",
                                q.getId());
                    }
                    Long userOption = userAnswers.get(q.getId());
                    boolean correct = userOption != null
                            && q.getOptions().stream()
                                    .anyMatch(o -> o.isCorrect() && Objects.equals(o.getId(), userOption));
                    return QuizValidationResult.QuestionResult.builder()
                            .questionId(q.getId())
                            .correct(correct)
                            .build();
                })
                .toList();

        long correctCount = results.stream().filter(QuizValidationResult.QuestionResult::isCorrect).count();
        int totalQuestions = assessment.getQuestions().size();
        int scorePercentage = totalQuestions > 0 ? (int) ((correctCount * 100) / totalQuestions) : 0;
        boolean passed = scorePercentage >= assessment.getPassPercentage();

        return Optional.of(QuizValidationResult.builder()
                .passed(passed)
                .scorePercentage(scorePercentage)
                .passPercentage(assessment.getPassPercentage())
                .results(results)
                .build());
    }
}
