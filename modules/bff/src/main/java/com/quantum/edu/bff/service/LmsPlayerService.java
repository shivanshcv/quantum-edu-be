package com.quantum.edu.bff.service;

import com.quantum.edu.bff.dto.LmsPlayerResponse;
import com.quantum.edu.catalogue.dto.QuizAnswerInput;
import com.quantum.edu.lms.api.LmsApi;
import com.quantum.edu.lms.dto.LmsPlayerData;
import com.quantum.edu.lms.dto.QuizValidationResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LmsPlayerService {

    private final LmsApi lmsApi;

    public LmsPlayerService(LmsApi lmsApi) {
        this.lmsApi = lmsApi;
    }

    public LmsPlayerResponse getPlayer(Long userId, Long productId) {
        LmsPlayerData data = lmsApi.getPlayerData(userId, productId);
        return toResponse(data);
    }

    public void markLessonComplete(Long userId, Long contentId) {
        lmsApi.markLessonComplete(userId, contentId);
    }

    public QuizValidationResponse validateQuizAnswers(Long userId, Long contentId, List<QuizAnswerInput> answers) {
        return lmsApi.validateQuizAnswers(userId, contentId, answers);
    }

    private LmsPlayerResponse toResponse(LmsPlayerData data) {
        List<LmsPlayerResponse.ModuleResponse> modules = data.getModules().stream()
                .map(this::toModuleResponse)
                .collect(Collectors.toList());

        return LmsPlayerResponse.builder()
                .courseSlug(data.getCourseSlug())
                .courseTitle(data.getCourseTitle())
                .subtitle(data.getSubtitle())
                .instructor(data.getInstructor())
                .completedLessons(data.getCompletedLessons())
                .totalLessons(data.getTotalLessons())
                .progressPercentage(data.getProgressPercentage())
                .modules(modules)
                .build();
    }

    private LmsPlayerResponse.ModuleResponse toModuleResponse(LmsPlayerData.ModuleData mod) {
        List<LmsPlayerResponse.LessonResponse> lessons = mod.getLessons().stream()
                .map(this::toLessonResponse)
                .collect(Collectors.toList());

        return LmsPlayerResponse.ModuleResponse.builder()
                .id(mod.getId())
                .title(mod.getTitle())
                .locked(mod.isLocked())
                .lessons(lessons)
                .build();
    }

    private LmsPlayerResponse.LessonResponse toLessonResponse(LmsPlayerData.LessonData lesson) {
        LmsPlayerResponse.AssessmentResponse assessment = null;
        if (lesson.getAssessment() != null) {
            assessment = LmsPlayerResponse.AssessmentResponse.builder()
                    .passPercentage(lesson.getAssessment().getPassPercentage())
                    .questions(lesson.getAssessment().getQuestions().stream()
                            .map(q -> LmsPlayerResponse.QuestionResponse.builder()
                                    .id(q.getId())
                                    .questionText(q.getQuestionText())
                                    .options(q.getOptions().stream()
                                            .map(o -> LmsPlayerResponse.OptionResponse.builder()
                                                    .id(o.getId())
                                                    .optionText(o.getOptionText())
                                                    .build())
                                            .toList())
                                    .build())
                            .toList())
                    .build();
        }

        return LmsPlayerResponse.LessonResponse.builder()
                .id(lesson.getContentId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .durationMinutes(lesson.getDurationMinutes())
                .status(lesson.getStatus())
                .url(lesson.getUrl())
                .moduleType(lesson.getModuleType())
                .type(lesson.getType())
                .assessment(assessment)
                .build();
    }
}
