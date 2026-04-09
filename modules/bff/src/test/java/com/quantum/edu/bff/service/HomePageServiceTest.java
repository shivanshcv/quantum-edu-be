package com.quantum.edu.bff.service;

import com.quantum.edu.bff.config.HeroSectionProperties;
import com.quantum.edu.catalogue.api.ProductCatalogueApi;
import com.quantum.edu.common.config.CurrencyProperties;
import com.quantum.edu.common.util.CurrencyFormatter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HomePageServiceTest {

    @Test
    void getHomePage_doesNotThrowWhenHeadlineAndImageAreNull() {
        HeroSectionProperties hero = new HeroSectionProperties();
        hero.setTitle("T");
        hero.setHeadline(null);
        hero.setImage(null);

        ProductCatalogueApi api = mock(ProductCatalogueApi.class);
        when(api.getFeaturedProducts()).thenReturn(List.of());

        CurrencyFormatter fmt = new CurrencyFormatter(new CurrencyProperties());

        HomePageService svc = new HomePageService(hero, api, fmt);
        assertDoesNotThrow(() -> {
            var page = svc.getHomePage();
            assertNotNull(page.getMain());
        });
    }
}
