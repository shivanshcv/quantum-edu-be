package com.quantum.edu.catalogue.service;

import com.quantum.edu.catalogue.domain.Assessment;
import com.quantum.edu.catalogue.domain.AssessmentOption;
import com.quantum.edu.catalogue.domain.AssessmentQuestion;
import com.quantum.edu.catalogue.dto.QuizAnswerInput;
import com.quantum.edu.catalogue.dto.QuizValidationResult;
import com.quantum.edu.catalogue.repository.AssessmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QuizValidationServiceTest {

    @Mock
    private AssessmentRepository assessmentRepository;

    private QuizValidationService service;

    @BeforeEach
    void setUp() {
        service = new QuizValidationService(assessmentRepository);
    }

    @Test
    void validateAnswers_passesWhenUserSelectsMarkedCorrectOption() {
        AssessmentOption wrong = mockOption(1L, false);
        AssessmentOption right = mockOption(2L, true);
        AssessmentQuestion q = mockQuestion(10L, new ArrayList<>(List.of(wrong, right)));
        Assessment assessment = mockAssessment(70, new ArrayList<>(List.of(q)));

        when(assessmentRepository.findByProductContentIdWithQuestionsAndOptions(99L))
                .thenReturn(Optional.of(assessment));

        Optional<QuizValidationResult> result = service.validateAnswers(
                99L,
                List.of(new QuizAnswerInput(10L, 2L)));

        assertThat(result).isPresent();
        assertThat(result.get().isPassed()).isTrue();
        assertThat(result.get().getScorePercentage()).isEqualTo(100);
        assertThat(result.get().getResults().get(0).isCorrect()).isTrue();
    }

    @Test
    void validateAnswers_failsWhenUserSelectsWrongOption() {
        AssessmentOption wrong = mockOption(1L, false);
        AssessmentOption right = mockOption(2L, true);
        AssessmentQuestion q = mockQuestion(10L, new ArrayList<>(List.of(wrong, right)));
        Assessment assessment = mockAssessment(70, new ArrayList<>(List.of(q)));

        when(assessmentRepository.findByProductContentIdWithQuestionsAndOptions(99L))
                .thenReturn(Optional.of(assessment));

        Optional<QuizValidationResult> result = service.validateAnswers(
                99L,
                List.of(new QuizAnswerInput(10L, 1L)));

        assertThat(result).isPresent();
        assertThat(result.get().isPassed()).isFalse();
        assertThat(result.get().getScorePercentage()).isEqualTo(0);
    }

    private static AssessmentOption mockOption(long id, boolean correct) {
        AssessmentOption o = org.mockito.Mockito.mock(AssessmentOption.class);
        when(o.getId()).thenReturn(id);
        when(o.isCorrect()).thenReturn(correct);
        return o;
    }

    private static AssessmentQuestion mockQuestion(long id, List<AssessmentOption> options) {
        AssessmentQuestion q = org.mockito.Mockito.mock(AssessmentQuestion.class);
        when(q.getId()).thenReturn(id);
        when(q.getOptions()).thenReturn(options);
        return q;
    }

    private static Assessment mockAssessment(int passPercent, List<AssessmentQuestion> questions) {
        Assessment a = org.mockito.Mockito.mock(Assessment.class);
        when(a.getPassPercentage()).thenReturn(passPercent);
        when(a.getQuestions()).thenReturn(questions);
        return a;
    }
}
