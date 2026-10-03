package com.quantum.edu.bff.util;

import com.quantum.edu.bff.dto.PriceDetailsResponse;
import com.quantum.edu.common.util.CurrencyFormatter;

import java.math.BigDecimal;

/**
 * Builds {@link PriceDetailsResponse} for catalogue-style surfaces (home grid, catalog, PDP).
 * Cart and verify flows keep their own numeric fields; this factory is not used there.
 */
public final class ProductPriceDetailsFactory {

    private ProductPriceDetailsFactory() {
    }

    /**
     * When {@link #isSalePrice(BigDecimal, BigDecimal)} is true, {@code originalPrice} is the formatted
     * list/MRP and {@code price} is the formatted amount the customer pays. Otherwise {@code originalPrice}
     * is omitted and {@code price} follows the legacy single-field display rule.
     */
    public static PriceDetailsResponse forCatalogSurface(BigDecimal listPrice, BigDecimal discountPrice,
                                                         CurrencyFormatter currencyFormatter) {
        BigDecimal list = listPrice != null ? listPrice : BigDecimal.ZERO;
        if (isSalePrice(list, discountPrice)) {
            return PriceDetailsResponse.builder()
                    .originalPrice(currencyFormatter.format(list))
                    .price(currencyFormatter.format(discountPrice))
                    .build();
        }
        BigDecimal display = legacyDisplayPrice(list, discountPrice);
        return PriceDetailsResponse.builder()
                .price(currencyFormatter.format(display))
                .build();
    }

    /**
     * A genuine sale: positive discount strictly below positive list price.
     */
    static boolean isSalePrice(BigDecimal listPrice, BigDecimal discountPrice) {
        if (discountPrice == null || discountPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (listPrice == null || listPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return discountPrice.compareTo(listPrice) < 0;
    }

    /**
     * Matches pre-change BFF behavior: prefer discount when {@code > 0}, else list.
     */
    private static BigDecimal legacyDisplayPrice(BigDecimal listPrice, BigDecimal discountPrice) {
        if (discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0) {
            return discountPrice;
        }
        return listPrice != null ? listPrice : BigDecimal.ZERO;
    }
}
