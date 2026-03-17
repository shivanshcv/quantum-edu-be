package com.quantum.edu.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutItemRequest {
    @NotNull
    private Long productId;
    @NotNull
    private BigDecimal price;
    private BigDecimal discountPrice;
    @NotNull
    private BigDecimal finalPrice;
    @NotNull
    private BigDecimal gstAmount;
    private boolean free;
}
