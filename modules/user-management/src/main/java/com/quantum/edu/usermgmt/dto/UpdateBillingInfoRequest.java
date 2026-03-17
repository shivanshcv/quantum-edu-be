package com.quantum.edu.usermgmt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBillingInfoRequest {

    @NotBlank(message = "billingName is required")
    @Size(max = 255)
    private String billingName;

    @NotBlank(message = "billingAddressLine1 is required")
    @Size(max = 255)
    private String billingAddressLine1;

    @Size(max = 255)
    private String billingAddressLine2;

    @NotBlank(message = "billingCity is required")
    @Size(max = 100)
    private String billingCity;

    @NotBlank(message = "billingState is required")
    @Size(max = 100)
    private String billingState;

    @NotBlank(message = "billingCountry is required")
    @Size(max = 100)
    private String billingCountry;

    @NotBlank(message = "billingPostalCode is required")
    @Size(max = 20)
    private String billingPostalCode;

    @Size(max = 30)
    private String billingGstNumber;
}
