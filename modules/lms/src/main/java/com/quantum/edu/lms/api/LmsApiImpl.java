package com.quantum.edu.lms.api;

import com.quantum.edu.catalogue.dto.QuizAnswerInput;
import com.quantum.edu.lms.dto.LmsPlayerData;
import com.quantum.edu.lms.dto.QuizValidationResponse;
import com.quantum.edu.lms.service.LmsService;
import com.quantum.edu.lms.service.ProgressService;
import com.quantum.edu.lms.service.QuizService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LmsApiImpl implements LmsApi {

    private final LmsService lmsService;
    private final ProgressService progressService;
    private final QuizService quizService;

    public LmsApiImpl(LmsService lmsService, ProgressService progressService, QuizService quizService) {
        this.lmsService = lmsService;
        this.progressService = progressService;
        this.quizService = quizService;
    }

    @Override
    public LmsPlayerData getPlayerData(Long userId, Long productId) {
        return lmsService.getPlayerData(userId, productId);
    }

    @Override
    public void markLessonComplete(Long userId, Long contentId) {
        progressService.markComplete(userId, contentId);
    }

    @Override
    public QuizValidationResponse validateQuizAnswers(Long userId, Long contentId, List<QuizAnswerInput> answers) {
        return quizService.validateAndStore(userId, contentId, answers);
    }
}
