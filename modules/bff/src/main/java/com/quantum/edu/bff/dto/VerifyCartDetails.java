package com.quantum.edu.bff.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyCartDetails {
    private String badge;
    private VerifyCartBillingDetails billingDetails;
    private VerifyCartEmailVerification emailVerification;
    private String title;
    private String subtitle;
    private String courseListTitle;
    private String orderSummaryTitle;
    private String subtotalLabel;
    private String taxesLabel;
    private String totalLabel;
    private String payNowLabel;
    private String emptyStateMessage;
    private String loadingMessage;
    private String errorMessage;
    private List<VerifyCartItemDto> items;
    private String subtotal;
    private String taxes;
    private String total;
}
