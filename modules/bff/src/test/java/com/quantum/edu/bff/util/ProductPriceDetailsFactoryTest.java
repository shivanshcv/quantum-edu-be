package com.quantum.edu.bff.util;

import com.quantum.edu.bff.dto.PriceDetailsResponse;
import com.quantum.edu.common.config.CurrencyProperties;
import com.quantum.edu.common.util.CurrencyFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductPriceDetailsFactoryTest {

    private CurrencyFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new CurrencyFormatter(new CurrencyProperties());
    }

    @Test
    void forCatalogSurface_sale_setsOriginalAndDiscountedStrings() {
        PriceDetailsResponse r = ProductPriceDetailsFactory.forCatalogSurface(
                new BigDecimal("100.00"), new BigDecimal("80.00"), formatter);
        assertNotNull(r.getOriginalPrice());
        assertNotNull(r.getPrice());
        assertTrue(r.getOriginalPrice().length() > 0);
        assertTrue(r.getPrice().length() > 0);
    }

    @Test
    void forCatalogSurface_noDiscount_omitsOriginalPrice() {
        PriceDetailsResponse r = ProductPriceDetailsFactory.forCatalogSurface(
                new BigDecimal("100.00"), null, formatter);
        assertNull(r.getOriginalPrice());
        assertNotNull(r.getPrice());
    }

    @Test
    void forCatalogSurface_discountZero_omitsOriginalPrice() {
        PriceDetailsResponse r = ProductPriceDetailsFactory.forCatalogSurface(
                new BigDecimal("100.00"), BigDecimal.ZERO, formatter);
        assertNull(r.getOriginalPrice());
        assertNotNull(r.getPrice());
    }

    @Test
    void forCatalogSurface_discountEqualsList_notTreatedAsSale() {
        PriceDetailsResponse r = ProductPriceDetailsFactory.forCatalogSurface(
                new BigDecimal("100.00"), new BigDecimal("100.00"), formatter);
        assertNull(r.getOriginalPrice());
        assertNotNull(r.getPrice());
    }

    @Test
    void isSalePrice_trueOnlyWhenDiscountBelowPositiveList() {
        assertTrue(ProductPriceDetailsFactory.isSalePrice(new BigDecimal("100"), new BigDecimal("99")));
        assertFalse(ProductPriceDetailsFactory.isSalePrice(new BigDecimal("100"), new BigDecimal("100")));
        assertFalse(ProductPriceDetailsFactory.isSalePrice(new BigDecimal("100"), new BigDecimal("101")));
        assertFalse(ProductPriceDetailsFactory.isSalePrice(null, new BigDecimal("50")));
        assertFalse(ProductPriceDetailsFactory.isSalePrice(new BigDecimal("100"), null));
        assertFalse(ProductPriceDetailsFactory.isSalePrice(new BigDecimal("100"), BigDecimal.ZERO));
    }
}
