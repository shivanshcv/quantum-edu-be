package com.quantum.edu.bff.service;

import com.quantum.edu.bff.dto.*;
import com.quantum.edu.cart.api.CartApi;
import com.quantum.edu.cart.dto.CartItemResponse;
import com.quantum.edu.catalogue.api.ProductCatalogueApi;
import com.quantum.edu.catalogue.dto.ProductDetailResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartPageService {

    private final CartApi cartApi;
    private final ProductCatalogueApi productCatalogueApi;

    public CartPageService(CartApi cartApi, ProductCatalogueApi productCatalogueApi) {
        this.cartApi = cartApi;
        this.productCatalogueApi = productCatalogueApi;
    }

    public PageResponse getCartPage(Long userId) {
        var cartResponse = cartApi.getCart(userId);

        List<CartItemWithProductDto> items = new ArrayList<>();
        if (cartResponse.getItems() != null) {
            for (CartItemResponse cartItem : cartResponse.getItems()) {
                ProductDetailResponse product = productCatalogueApi.getPublishedProductById(cartItem.getProductId());
                CartItemWithProductDto.ProductSummary productSummary = CartItemWithProductDto.ProductSummary.builder()
                        .id(product.getId())
                        .title(product.getTitle())
                        .slug(product.getSlug())
                        .shortDescription(product.getShortDescription())
                        .price(product.getPrice())
                        .discountPrice(product.getDiscountPrice())
                        .thumbnailUrl(product.getThumbnailUrl())
                        .difficultyLevel(product.getDifficultyLevel())
                        .durationMinutes(product.getDurationMinutes())
                        .build();
                items.add(CartItemWithProductDto.builder()
                        .productId(cartItem.getProductId())
                        .addedAt(cartItem.getAddedAt())
                        .product(productSummary)
                        .build());
            }
        }

        CartPageDetails details = CartPageDetails.builder()
                .items(items)
                .build();

        ComponentResponse component = ComponentResponse.builder()
                .type("CART")
                .config(java.util.Map.of("theme", "light"))
                .details(details)
                .build();

        MainSection main = MainSection.builder()
                .type("CART")
                .components(List.of(component))
                .data(java.util.Map.of())
                .build();

        return PageResponse.builder()
                .main(main)
                .build();
    }
}
