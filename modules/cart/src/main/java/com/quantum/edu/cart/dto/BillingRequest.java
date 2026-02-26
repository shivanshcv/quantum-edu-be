package com.quantum.edu.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingRequest {
    @NotBlank
    private String billingName;
    @NotBlank
    private String billingAddressLine1;
    private String billingAddressLine2;
    @NotBlank
    private String billingCity;
    @NotBlank
    private String billingState;
    @NotBlank
    private String billingCountry;
    @NotBlank
    private String billingPostalCode;
    private String billingGstNumber;
}
