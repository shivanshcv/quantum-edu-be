package com.quantum.edu.catalogue.controller;

import com.quantum.edu.catalogue.dto.CreateModuleRequest;
import com.quantum.edu.catalogue.dto.ModuleResponse;
import com.quantum.edu.catalogue.service.ProductModuleService;
import com.quantum.edu.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/catalogue/products/{productId}")
public class AdminProductModuleController {

    private final ProductModuleService moduleService;

    public AdminProductModuleController(ProductModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @PostMapping("/createModule")
    public ResponseEntity<ApiResponse<ModuleResponse>> createModule(
            @PathVariable Long productId,
            @Valid @RequestBody CreateModuleRequest request) {
        ModuleResponse response = moduleService.create(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/updateModule/{moduleId}")
    public ResponseEntity<ApiResponse<ModuleResponse>> updateModule(
            @PathVariable Long productId,
            @PathVariable Long moduleId,
            @Valid @RequestBody CreateModuleRequest request) {
        ModuleResponse response = moduleService.update(moduleId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/deleteModule/{moduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteModule(
            @PathVariable Long productId,
            @PathVariable Long moduleId) {
        moduleService.delete(moduleId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/reorderModules")
    public ResponseEntity<ApiResponse<List<ModuleResponse>>> reorderModules(
            @PathVariable Long productId,
            @RequestBody List<Long> moduleIds) {
        List<ModuleResponse> response = moduleService.reorder(productId, moduleIds);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/getModules")
    public ResponseEntity<ApiResponse<List<ModuleResponse>>> getModules(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(moduleService.listByProduct(productId)));
    }
}
