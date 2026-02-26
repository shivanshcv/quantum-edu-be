package com.quantum.edu.catalogue.controller;

import com.quantum.edu.catalogue.dto.*;
import com.quantum.edu.catalogue.service.ProductContentService;
import com.quantum.edu.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/catalogue")
public class AdminProductContentController {

    private final ProductContentService contentService;

    public AdminProductContentController(ProductContentService contentService) {
        this.contentService = contentService;
    }

    @PostMapping("/addProductContent/{productId}")
    public ResponseEntity<ApiResponse<ContentResponse>> addProductContent(
            @PathVariable Long productId,
            @Valid @RequestBody CreateContentRequest request) {
        ContentResponse response = contentService.addContent(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/updateProductContent/{productId}/{contentId}")
    public ResponseEntity<ApiResponse<ContentResponse>> updateProductContent(
            @PathVariable Long productId,
            @PathVariable Long contentId,
            @Valid @RequestBody UpdateContentRequest request) {
        ContentResponse response = contentService.updateContent(productId, contentId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/deleteProductContent/{productId}/{contentId}")
    public ResponseEntity<ApiResponse<Void>> deleteProductContent(
            @PathVariable Long productId,
            @PathVariable Long contentId) {
        contentService.deleteContent(productId, contentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/reorderProductContent/{productId}")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> reorderProductContent(
            @PathVariable Long productId,
            @Valid @RequestBody ReorderContentRequest request) {
        List<ContentResponse> response = contentService.reorder(productId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/getProductContent/{productId}")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getProductContent(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(contentService.listContent(productId)));
    }

    @PostMapping("/addAssessmentQuestion/{contentId}")
    public ResponseEntity<ApiResponse<QuestionResponse>> addAssessmentQuestion(
            @PathVariable Long contentId,
            @Valid @RequestBody CreateQuestionRequest request) {
        QuestionResponse response = contentService.addQuestion(contentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/updateAssessmentQuestion/{contentId}/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateAssessmentQuestion(
            @PathVariable Long contentId,
            @PathVariable Long questionId,
            @Valid @RequestBody CreateQuestionRequest request) {
        QuestionResponse response = contentService.updateQuestion(contentId, questionId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/deleteAssessmentQuestion/{contentId}/{questionId}")
    public ResponseEntity<ApiResponse<Void>> deleteAssessmentQuestion(
            @PathVariable Long contentId,
            @PathVariable Long questionId) {
        contentService.deleteQuestion(contentId, questionId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
