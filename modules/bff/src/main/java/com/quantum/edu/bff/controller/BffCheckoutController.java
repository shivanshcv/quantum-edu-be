package com.quantum.edu.bff.controller;

import com.quantum.edu.bff.dto.BffCheckoutRequest;
import com.quantum.edu.bff.service.BffCheckoutService;
import com.quantum.edu.cart.dto.CheckoutResponse;
import com.quantum.edu.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bff")
public class BffCheckoutController {

    private final BffCheckoutService bffCheckoutService;

    public BffCheckoutController(BffCheckoutService bffCheckoutService) {
        this.bffCheckoutService = bffCheckoutService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody BffCheckoutRequest request) {
        CheckoutResponse response = bffCheckoutService.checkout(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
