package com.quantum.edu.bff.dto;

import com.quantum.edu.cart.dto.BillingRequest;
import com.quantum.edu.cart.dto.CheckoutItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BffCheckoutRequest {
    /**
     * If true, billing was already in user profile (from verify response). BFF uses billing from request without saving.
     * If false, FE collected billing from user. BFF saves to user profile first, then proceeds with checkout.
     */
    private boolean billingInfoPresent;

    @NotEmpty(message = "items cannot be empty")
    @Valid
    private List<CheckoutItemRequest> items;

    @NotNull
    @Valid
    private BillingRequest billing;
}
