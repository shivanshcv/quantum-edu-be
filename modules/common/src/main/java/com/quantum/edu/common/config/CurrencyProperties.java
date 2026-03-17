package com.quantum.edu.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.currency")
@Getter
@Setter
public class CurrencyProperties {

    /**
     * ISO 4217 currency code (e.g. INR, USD). Used for Razorpay and verify/checkout responses.
     */
    private String code = "INR";

    /**
     * Currency symbol for display (e.g. ₹, $).
     */
    private String symbol = "₹";
}
