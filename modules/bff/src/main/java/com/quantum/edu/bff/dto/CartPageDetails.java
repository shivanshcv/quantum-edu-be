package com.quantum.edu.bff.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartPageDetails {
    private List<CartItemWithProductDto> items;
    /** Sum of effective prices (discountPrice if set, else price) for all items. Raw decimal for calculations. */
    private BigDecimal subtotal;
    /** Formatted subtotal for display (e.g. ₹2,098). */
    private String subtotalFormatted;
}
