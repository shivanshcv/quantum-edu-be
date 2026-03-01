package com.quantum.edu.cart.api;

import com.quantum.edu.cart.dto.CartResponse;
import com.quantum.edu.cart.dto.CheckoutRequest;
import com.quantum.edu.cart.dto.CheckoutResponse;
import com.quantum.edu.cart.dto.VerifyRequest;
import com.quantum.edu.cart.dto.VerifyResponse;
import com.quantum.edu.cart.service.CartService;
import com.quantum.edu.cart.service.OrderService;
import com.quantum.edu.cart.service.VerifyService;
import org.springframework.stereotype.Component;

@Component
public class CartApiImpl implements CartApi {

    private final CartService cartService;
    private final VerifyService verifyService;
    private final OrderService orderService;

    public CartApiImpl(CartService cartService, VerifyService verifyService, OrderService orderService) {
        this.cartService = cartService;
        this.verifyService = verifyService;
        this.orderService = orderService;
    }

    @Override
    public CartResponse getCart(Long userId) {
        return cartService.getCart(userId);
    }

    @Override
    public VerifyResponse verify(Long userId, VerifyRequest request) {
        return verifyService.verify(userId, request);
    }

    @Override
    public CheckoutResponse checkout(Long userId, CheckoutRequest request) {
        return orderService.checkout(userId, request);
    }
}
