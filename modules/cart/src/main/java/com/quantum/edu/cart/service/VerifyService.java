package com.quantum.edu.cart.service;

import com.quantum.edu.cart.dto.*;
import com.quantum.edu.cart.repository.CartRepository;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import com.quantum.edu.ownership.api.OwnershipApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VerifyService {

    private static final String CURRENCY = "INR";

    private final CartRepository cartRepository;
    private final OwnershipApi ownershipApi;

    @Value("${cart.gst-rate:0.18}")
    private double gstRate;

    public VerifyService(CartRepository cartRepository, OwnershipApi ownershipApi) {
        this.cartRepository = cartRepository;
        this.ownershipApi = ownershipApi;
    }

    public VerifyResponse verify(Long userId, VerifyRequest request) {
        List<Long> cartProductIds = cartRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(c -> c.getProductId())
                .collect(Collectors.toList());

        if (cartProductIds.isEmpty()) {
            throw new InternalException(InternalErrorCode.CART_EMPTY);
        }

        Set<Long> cartSet = Set.copyOf(cartProductIds);
        List<VerifyItemResponse> items = new ArrayList<>();
        List<AlreadyOwnedItemResponse> alreadyOwned = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal gstTotal = BigDecimal.ZERO;

        for (VerifyItemRequest req : request.getItems()) {
            if (!cartSet.contains(req.getProductId())) {
                continue;
            }
            if (ownershipApi.ownsCourse(userId, req.getProductId())) {
                alreadyOwned.add(AlreadyOwnedItemResponse.builder()
                        .productId(req.getProductId())
                        .title(req.getTitle())
                        .reason("ALREADY_OWNED")
                        .build());
                continue;
            }
            BigDecimal finalPrice = req.getDiscountPrice() != null && req.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0
                    ? req.getDiscountPrice()
                    : req.getPrice();
            BigDecimal gstAmount = finalPrice.multiply(BigDecimal.valueOf(gstRate)).setScale(2, RoundingMode.HALF_UP);

            items.add(VerifyItemResponse.builder()
                    .productId(req.getProductId())
                    .title(req.getTitle())
                    .price(req.getPrice())
                    .discountPrice(req.getDiscountPrice())
                    .finalPrice(finalPrice)
                    .gstAmount(gstAmount)
                    .quantity(1)
                    .build());

            subtotal = subtotal.add(finalPrice);
            gstTotal = gstTotal.add(gstAmount);
        }

        if (items.isEmpty() && !alreadyOwned.isEmpty()) {
            throw new InternalException(InternalErrorCode.CART_ALL_ITEMS_OWNED);
        }
        if (items.isEmpty()) {
            throw new InternalException(InternalErrorCode.CART_EMPTY);
        }

        BigDecimal finalAmount = subtotal.add(gstTotal);

        return VerifyResponse.builder()
                .items(items)
                .alreadyOwned(alreadyOwned)
                .subtotal(subtotal)
                .gstAmount(gstTotal)
                .finalAmount(finalAmount)
                .currency(CURRENCY)
                .build();
    }
}
