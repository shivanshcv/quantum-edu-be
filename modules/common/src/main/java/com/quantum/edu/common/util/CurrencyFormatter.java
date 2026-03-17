package com.quantum.edu.common.util;

import com.quantum.edu.common.config.CurrencyProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Centralized currency formatting. Uses app.currency config for consistent display across all flows.
 */
@Component
public class CurrencyFormatter {

    private final CurrencyProperties currencyProperties;
    private final DecimalFormat displayFormat;
    private final DecimalFormat displayFormatWithDecimals;

    public CurrencyFormatter(CurrencyProperties currencyProperties) {
        this.currencyProperties = currencyProperties;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        this.displayFormat = new DecimalFormat("#,##0", symbols);
        this.displayFormatWithDecimals = new DecimalFormat("#,##0.00", symbols);
    }

    /**
     * Format amount for card/display (e.g. "₹1,999").
     */
    public String format(BigDecimal amount) {
        if (amount == null) {
            return currencyProperties.getSymbol() + "0";
        }
        BigDecimal rounded = amount.setScale(2, RoundingMode.HALF_UP);
        String formatted = rounded.stripTrailingZeros().scale() <= 0
                ? displayFormat.format(rounded)
                : displayFormatWithDecimals.format(rounded);
        return currencyProperties.getSymbol() + formatted;
    }

    /**
     * Format amount with currency code (e.g. "INR 1,999"). Used for verify subtotal/taxes/total.
     */
    public String formatWithCode(BigDecimal amount) {
        if (amount == null) {
            return currencyProperties.getCode() + " 0";
        }
        String formatted = displayFormat.format(amount.setScale(0, RoundingMode.HALF_UP));
        return currencyProperties.getCode() + " " + formatted;
    }

    /**
     * Get the configured currency code (e.g. INR).
     */
    public String getCurrencyCode() {
        return currencyProperties.getCode();
    }
}
