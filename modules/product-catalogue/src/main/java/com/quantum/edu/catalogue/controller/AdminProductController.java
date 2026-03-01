package com.quantum.edu.catalogue.controller;

import com.quantum.edu.catalogue.domain.ProductAttributes;
import com.quantum.edu.catalogue.dto.*;
import com.quantum.edu.catalogue.service.ProductPdpService;
import com.quantum.edu.catalogue.service.ProductService;
import com.quantum.edu.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/catalogue")
public class AdminProductController {

    private final ProductService productService;
    private final ProductPdpService pdpService;

    public AdminProductController(ProductService productService, ProductPdpService pdpService) {
        this.productService = productService;
        this.pdpService = pdpService;
    }

    @PostMapping("/createProduct")
    public ResponseEntity<ApiResponse<ProductListResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductListResponse response = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/updateProduct/{id}")
    public ResponseEntity<ApiResponse<ProductListResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductListResponse response = productService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/deleteProduct/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/updateProductPublish/{id}")
    public ResponseEntity<ApiResponse<ProductListResponse>> updateProductPublish(
            @PathVariable Long id,
            @Valid @RequestBody PublishRequest request) {
        ProductListResponse response = productService.setPublished(id, request.getPublished());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/setFeatured/{id}")
    public ResponseEntity<ApiResponse<ProductListResponse>> setFeatured(
            @PathVariable Long id,
            @Valid @RequestBody FeaturedRequest request) {
        ProductListResponse response = productService.setFeatured(id, request.getFeatured());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/getProducts")
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getProducts(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.listAll(pageable)));
    }

    @GetMapping("/getProduct/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(id)));
    }

    @PutMapping("/setAttributes/{id}")
    public ResponseEntity<ApiResponse<Void>> setAttributes(
            @PathVariable Long id,
            @RequestBody ProductAttributes attributes) {
        pdpService.setAttributes(id, attributes);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
