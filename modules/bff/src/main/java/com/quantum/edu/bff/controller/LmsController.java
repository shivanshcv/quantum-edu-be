package com.quantum.edu.bff.controller;

import com.quantum.edu.bff.dto.LmsPlayerResponse;
import com.quantum.edu.bff.service.LmsPlayerService;
import com.quantum.edu.catalogue.dto.QuizAnswerInput;
import com.quantum.edu.common.dto.ApiResponse;
import com.quantum.edu.lms.dto.QuizValidationResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lms")
public class LmsController {

    private final LmsPlayerService lmsPlayerService;

    public LmsController(LmsPlayerService lmsPlayerService) {
        this.lmsPlayerService = lmsPlayerService;
    }

    @GetMapping("/player")
    public ResponseEntity<ApiResponse<LmsPlayerResponse>> getPlayer(
            @RequestAttribute("userId") Long userId,
            @RequestParam Long productId) {
        LmsPlayerResponse response = lmsPlayerService.getPlayer(userId, productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/lessons/{contentId}/validate-quiz")
    public ResponseEntity<ApiResponse<QuizValidationResponse>> validateQuizAnswers(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long contentId,
            @Valid @RequestBody ValidateQuizRequest request) {
        QuizValidationResponse response = lmsPlayerService.validateQuizAnswers(userId, contentId, request.getAnswers());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/lessons/{contentId}/complete")
    public ResponseEntity<ApiResponse<Void>> markLessonComplete(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long contentId) {
        lmsPlayerService.markLessonComplete(userId, contentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @lombok.Getter
    @lombok.Setter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ValidateQuizRequest {
        @NotEmpty(message = "answers cannot be empty")
        private List<QuizAnswerInput> answers;
    }
}
