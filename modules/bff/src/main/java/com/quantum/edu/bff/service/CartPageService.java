package com.quantum.edu.bff.service;

import com.quantum.edu.bff.dto.*;
import com.quantum.edu.cart.api.CartApi;
import com.quantum.edu.cart.dto.CartItemResponse;
import com.quantum.edu.catalogue.api.ProductCatalogueApi;
import com.quantum.edu.catalogue.dto.ProductDetailResponse;
import com.quantum.edu.common.util.CurrencyFormatter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartPageService {

    private final CartApi cartApi;
    private final ProductCatalogueApi productCatalogueApi;
    private final CurrencyFormatter currencyFormatter;

    public CartPageService(CartApi cartApi, ProductCatalogueApi productCatalogueApi,
                           CurrencyFormatter currencyFormatter) {
        this.cartApi = cartApi;
        this.productCatalogueApi = productCatalogueApi;
        this.currencyFormatter = currencyFormatter;
    }

    public PageResponse getCartPage(Long userId) {
        var cartResponse = cartApi.getCart(userId);

        List<CartItemWithProductDto> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        if (cartResponse.getItems() != null) {
            for (CartItemResponse cartItem : cartResponse.getItems()) {
                ProductDetailResponse product = productCatalogueApi.getPublishedProductById(cartItem.getProductId());
                BigDecimal displayPrice = product.getDiscountPrice() != null
                        && product.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0
                        ? product.getDiscountPrice()
                        : product.getPrice();
                subtotal = subtotal.add(displayPrice);
                CartItemWithProductDto.ProductSummary productSummary = CartItemWithProductDto.ProductSummary.builder()
                        .id(product.getId())
                        .title(product.getTitle())
                        .slug(product.getSlug())
                        .shortDescription(product.getShortDescription())
                        .price(product.getPrice())
                        .discountPrice(product.getDiscountPrice())
                        .priceDetails(PriceDetailsResponse.builder()
                                .price(currencyFormatter.format(displayPrice))
                                .build())
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
                .subtotal(subtotal)
                .subtotalFormatted(currencyFormatter.format(subtotal))
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
