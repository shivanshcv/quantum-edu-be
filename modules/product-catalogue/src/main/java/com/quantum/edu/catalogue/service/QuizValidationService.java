package com.quantum.edu.catalogue.service;

import com.quantum.edu.catalogue.domain.Assessment;
import com.quantum.edu.catalogue.domain.AssessmentOption;
import com.quantum.edu.catalogue.domain.AssessmentQuestion;
import com.quantum.edu.catalogue.dto.QuizAnswerInput;
import com.quantum.edu.catalogue.dto.QuizValidationResult;
import com.quantum.edu.catalogue.repository.AssessmentRepository;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuizValidationService {

    private final AssessmentRepository assessmentRepository;

    public QuizValidationService(AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
    }

    @Transactional(readOnly = true)
    public Optional<QuizValidationResult> validateAnswers(Long productContentId, List<QuizAnswerInput> answers) {
        Assessment assessment = assessmentRepository.findByProductContentId(productContentId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.ASSESSMENT_NOT_FOUND));

        Map<Long, Long> correctOptionByQuestion = new HashMap<>();
        for (AssessmentQuestion q : assessment.getQuestions()) {
            Optional<Long> correctId = q.getOptions().stream()
                    .filter(AssessmentOption::isCorrect)
                    .map(AssessmentOption::getId)
                    .findFirst();
            correctId.ifPresent(id -> correctOptionByQuestion.put(q.getId(), id));
        }

        Map<Long, Long> userAnswers = answers.stream()
                .collect(Collectors.toMap(QuizAnswerInput::getQuestionId, QuizAnswerInput::getOptionId, (a, b) -> b));

        List<QuizValidationResult.QuestionResult> results = assessment.getQuestions().stream()
                .map(q -> {
                    Long userOption = userAnswers.get(q.getId());
                    Long correctOption = correctOptionByQuestion.get(q.getId());
                    boolean correct = correctOption != null && correctOption.equals(userOption);
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
