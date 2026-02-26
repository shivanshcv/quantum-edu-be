package com.quantum.edu.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutResponse {
    private String razorpayOrderId;
    private Long amount;
    private String currency;
    private String keyId;
    private List<Long> orderIds;
}
