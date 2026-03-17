package com.quantum.edu.cart.service;

import com.quantum.edu.cart.domain.CartItem;
import com.quantum.edu.cart.dto.AddToCartResponse;
import com.quantum.edu.cart.dto.CartItemResponse;
import com.quantum.edu.cart.dto.CartResponse;
import com.quantum.edu.cart.repository.CartRepository;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final EntityManager entityManager;

    public CartService(CartRepository cartRepository, EntityManager entityManager) {
        this.cartRepository = cartRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public AddToCartResponse addItems(Long userId, List<Long> productIds) {
        List<CartItemResponse> added = new ArrayList<>();
        for (Long productId : productIds) {
            var existing = cartRepository.findByUserIdAndProductId(userId, productId);
            if (existing.isPresent()) {
                added.add(CartItemResponse.builder()
                        .productId(productId)
                        .addedAt(existing.get().getCreatedAt())
                        .build());
                continue;
            }
            CartItem item = new CartItem(userId, productId);
            item = cartRepository.save(item);
            entityManager.refresh(item);
            added.add(CartItemResponse.builder()
                    .productId(productId)
                    .addedAt(item.getCreatedAt())
                    .build());
        }
        return AddToCartResponse.builder().items(added).build();
    }

    @Transactional
    public void removeItem(Long userId, Long productId) {
        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public CartResponse getCart(Long userId) {
        List<CartItemResponse> items = cartRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(c -> CartItemResponse.builder()
                        .productId(c.getProductId())
                        .addedAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return CartResponse.builder().items(items).build();
    }

    public List<Long> getCartProductIds(Long userId) {
        return cartRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toList());
    }
}
