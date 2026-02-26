package com.quantum.edu.catalogue.controller;

import com.quantum.edu.catalogue.domain.Product;
import com.quantum.edu.catalogue.dto.CategoryResponse;
import com.quantum.edu.catalogue.dto.ProductDetailResponse;
import com.quantum.edu.catalogue.dto.ProductListResponse;
import com.quantum.edu.catalogue.service.CategoryService;
import com.quantum.edu.catalogue.service.ProductService;
import com.quantum.edu.common.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogue")
public class PublicCatalogueController {

    private final CategoryService categoryService;
    private final ProductService productService;

    public PublicCatalogueController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping("/getCategories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getTree(true)));
    }

    @GetMapping("/getProducts")
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Product.DifficultyLevel difficulty,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductListResponse> page = productService.searchPublished(search, categoryId, difficulty, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/getProduct/{slug}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(@PathVariable String slug) {
        ProductDetailResponse response = productService.getPublishedBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/getProductDetails")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetails(
            @RequestParam Long productId) {
        ProductDetailResponse response = productService.getPublishedById(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
