package com.quantum.edu.lms.api;

import com.quantum.edu.catalogue.dto.QuizAnswerInput;
import com.quantum.edu.lms.dto.LmsPlayerData;
import com.quantum.edu.lms.dto.QuizValidationResponse;

import java.util.List;

public interface LmsApi {

    LmsPlayerData getPlayerData(Long userId, Long productId);

    void markLessonComplete(Long userId, Long contentId);

    QuizValidationResponse validateQuizAnswers(Long userId, Long contentId, List<QuizAnswerInput> answers);
}
