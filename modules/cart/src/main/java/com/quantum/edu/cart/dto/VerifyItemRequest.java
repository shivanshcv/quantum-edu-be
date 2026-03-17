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
public class VerifyItemRequest {
    @NotNull
    private Long productId;
    @NotNull
    private String title;
    @NotNull
    private BigDecimal price;
    private BigDecimal discountPrice;
    private boolean free;
}
