package com.quantum.edu.bff.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyCartBillingDetails {
    private boolean isAvailable;
    private String title;
    private BillingSection section;
    private BillingData billing;  // Present when isAvailable=true, FE sends back in checkout

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BillingSection {
        private List<BillingField> fields;
        private String submitLabel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BillingField {
        private String id;
        private String label;
        private String type;
        private String placeholder;
        private boolean required;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BillingData {
        private String billingName;
        private String billingAddressLine1;
        private String billingAddressLine2;
        private String billingCity;
        private String billingState;
        private String billingCountry;
        private String billingPostalCode;
        private String billingGstNumber;
    }
}
