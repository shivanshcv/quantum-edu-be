package com.quantum.edu.bff.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyCartItemDto {
    private Long productId;
    private String title;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal finalPrice;
    private BigDecimal gstAmount;
    private int quantity;
}
