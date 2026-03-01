package com.quantum.edu.cart.api;

import com.quantum.edu.cart.dto.CartResponse;
import com.quantum.edu.cart.dto.CheckoutRequest;
import com.quantum.edu.cart.dto.CheckoutResponse;
import com.quantum.edu.cart.dto.VerifyRequest;
import com.quantum.edu.cart.dto.VerifyResponse;

/**
 * Public API for Cart module. Consumed by BFF.
 */
public interface CartApi {

    CartResponse getCart(Long userId);

    VerifyResponse verify(Long userId, VerifyRequest request);

    CheckoutResponse checkout(Long userId, CheckoutRequest request);
}
