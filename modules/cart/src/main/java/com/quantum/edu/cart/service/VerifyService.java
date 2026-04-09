package com.quantum.edu.cart.service;

import com.quantum.edu.cart.dto.*;
import com.quantum.edu.cart.repository.CartRepository;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import com.quantum.edu.common.util.CurrencyFormatter;
import com.quantum.edu.ownership.api.OwnershipApi;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VerifyService {

    private final CartRepository cartRepository;
    private final OwnershipApi ownershipApi;
    private final CurrencyFormatter currencyFormatter;

    public VerifyService(CartRepository cartRepository, OwnershipApi ownershipApi,
                        CurrencyFormatter currencyFormatter) {
        this.cartRepository = cartRepository;
        this.ownershipApi = ownershipApi;
        this.currencyFormatter = currencyFormatter;
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
            boolean isFree = req.isFree();
            BigDecimal finalPrice = isFree ? BigDecimal.ZERO
                    : (req.getDiscountPrice() != null && req.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0
                    ? req.getDiscountPrice()
                    : req.getPrice());

            items.add(VerifyItemResponse.builder()
                    .productId(req.getProductId())
                    .title(req.getTitle())
                    .price(req.getPrice())
                    .discountPrice(req.getDiscountPrice())
                    .finalPrice(finalPrice)
                    .quantity(1)
                    .free(isFree)
                    .build());

            subtotal = subtotal.add(finalPrice);
        }

        if (items.isEmpty() && !alreadyOwned.isEmpty()) {
            throw new InternalException(InternalErrorCode.CART_ALL_ITEMS_OWNED);
        }
        if (items.isEmpty()) {
            throw new InternalException(InternalErrorCode.CART_EMPTY);
        }

        boolean paymentRequired = subtotal.compareTo(BigDecimal.ZERO) > 0;

        return VerifyResponse.builder()
                .items(items)
                .alreadyOwned(alreadyOwned)
                .subtotal(subtotal)
                .finalAmount(subtotal)
                .currency(currencyFormatter.getCurrencyCode())
                .paymentRequired(paymentRequired)
                .build();
    }
}
