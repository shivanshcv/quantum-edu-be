package com.quantum.edu.cart.controller;

import com.quantum.edu.cart.dto.*;
import com.quantum.edu.cart.service.CartService;
import com.quantum.edu.cart.service.OrderService;
import com.quantum.edu.cart.service.VerifyService;
import com.quantum.edu.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;
    private final VerifyService verifyService;
    private final OrderService orderService;

    public CartController(CartService cartService, VerifyService verifyService, OrderService orderService) {
        this.cartService = cartService;
        this.verifyService = verifyService;
        this.orderService = orderService;
    }

    @PostMapping("/addItems")
    public ResponseEntity<ApiResponse<AddToCartResponse>> addItems(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody AddToCartRequest request) {
        AddToCartResponse response = cartService.addItems(userId, request.getProductIds());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/removeItem/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long productId) {
        cartService.removeItem(userId, productId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/getCart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestAttribute("userId") Long userId) {
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verifyCart")
    public ResponseEntity<ApiResponse<VerifyResponse>> verifyCart(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody VerifyRequest request) {
        VerifyResponse response = verifyService.verify(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CheckoutRequest request) {
        CheckoutResponse response = orderService.checkout(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
