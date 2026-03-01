package com.quantum.edu.bff.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BffVerifyCartRequest {

    @NotEmpty(message = "items cannot be empty")
    @Valid
    private List<CartItemRequest> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemRequest {
        @NotNull
        private Long productId;
        /**
         * Final product price that FE has shown to the user. BFF validates this matches the actual product price in BE.
         */
        @NotNull
        private BigDecimal price;
    }
}
