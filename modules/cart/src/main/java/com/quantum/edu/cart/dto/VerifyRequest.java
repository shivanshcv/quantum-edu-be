package com.quantum.edu.cart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class VerifyRequest {
    @NotEmpty(message = "items cannot be empty")
    @Valid
    private List<VerifyItemRequest> items;
    private String billingGstNumber;
}
