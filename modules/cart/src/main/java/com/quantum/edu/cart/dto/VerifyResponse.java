package com.quantum.edu.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyResponse {
    private List<VerifyItemResponse> items;
    private List<AlreadyOwnedItemResponse> alreadyOwned;
    private BigDecimal subtotal;
    private BigDecimal gstAmount;
    private BigDecimal finalAmount;
    private String currency;
    private boolean paymentRequired;
}
