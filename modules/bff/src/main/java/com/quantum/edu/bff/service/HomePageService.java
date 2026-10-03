package com.quantum.edu.bff.service;

import com.quantum.edu.bff.config.HeroSectionProperties;
import com.quantum.edu.bff.dto.*;
import com.quantum.edu.catalogue.api.ProductCatalogueApi;
import com.quantum.edu.catalogue.dto.ProductListResponse;
import com.quantum.edu.bff.util.ProductPriceDetailsFactory;
import com.quantum.edu.common.util.CurrencyFormatter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HomePageService {

    private static final String CARD_GRID_TITLE = "Industry Leading Accelerators";
    private static final String CARD_GRID_SUBTITLE = "Engineered by specialists with decades of experience.";
    private static final String CARD_GRID_BADGE = "OUR PATHWAYS";

    private final HeroSectionProperties heroProps;
    private final ProductCatalogueApi productCatalogueApi;
    private final CurrencyFormatter currencyFormatter;

    public HomePageService(HeroSectionProperties heroProps, ProductCatalogueApi productCatalogueApi,
                           CurrencyFormatter currencyFormatter) {
        this.heroProps = heroProps;
        this.productCatalogueApi = productCatalogueApi;
        this.currencyFormatter = currencyFormatter;
    }

    public PageResponse getHomePage() {
        ComponentResponse heroComponent = buildHeroSection();
        ComponentResponse cardGridComponent = buildCardGrid();

        MainSection main = MainSection.builder()
                .type("HOME")
                .components(List.of(heroComponent, cardGridComponent))
                .data(Map.of())
                .build();

        return PageResponse.builder().main(main).build();
    }

    private ComponentResponse buildHeroSection() {
        HeroSectionDetails details = HeroSectionDetails.builder()
                .title(heroProps.getTitle())
                .headline(buildHeadlineResponse())
                .subtitle(heroProps.getSubtitle())
                .badge(heroProps.getBadge())
                .image(buildHeroImageResponse())
                .stats(heroProps.getStats() != null
                        ? heroProps.getStats().stream().map(s -> StatResponse.builder()
                                .id(s.getId()).value(s.getValue()).label(s.getLabel()).icon(s.getIcon())
                                .build()).toList()
                        : List.of())
                .floatingCards(heroProps.getFloatingCards() != null
                        ? heroProps.getFloatingCards().stream().map(f -> FloatingCardResponse.builder()
                                .id(f.getId()).title(f.getTitle()).subtitle(f.getSubtitle()).position(f.getPosition())
                                .build()).toList()
                        : List.of())
                .ctas(heroProps.getCtas() != null
                        ? heroProps.getCtas().stream().map(c -> CtaResponse.builder()
                                .label(c.getLabel()).url(c.getUrl()).variant(c.getVariant()).type(c.getType())
                                .build()).toList()
                        : List.of())
                .build();

        return ComponentResponse.builder()
                .type("HERO_SECTION")
                .config(Map.of("theme", "dark", "padding", "large"))
                .details(details)
                .build();
    }

    private HeadlineResponse buildHeadlineResponse() {
        HeroSectionProperties.Headline h = heroProps.getHeadline();
        if (h == null) {
            return HeadlineResponse.builder().line1("").line2("").highlightWord("").build();
        }
        return HeadlineResponse.builder()
                .line1(h.getLine1() != null ? h.getLine1() : "")
                .line2(h.getLine2() != null ? h.getLine2() : "")
                .highlightWord(h.getHighlightWord() != null ? h.getHighlightWord() : "")
                .build();
    }

    private ImageResponse buildHeroImageResponse() {
        HeroSectionProperties.Image img = heroProps.getImage();
        if (img == null) {
            return ImageResponse.builder().src("").alt("").build();
        }
        return ImageResponse.builder()
                .src(img.getSrc() != null ? img.getSrc() : "")
                .alt(img.getAlt() != null ? img.getAlt() : "")
                .build();
    }

    private ComponentResponse buildCardGrid() {
        List<ProductListResponse> featured = productCatalogueApi.getFeaturedProducts();

        List<CardSection> sections = featured.stream()
                .map(this::toCardSection)
                .toList();

        CardGridDetails details = CardGridDetails.builder()
                .title(CARD_GRID_TITLE)
                .subtitle(CARD_GRID_SUBTITLE)
                .badge(CARD_GRID_BADGE)
                .sections(sections)
                .cta(CtaResponse.builder()
                        .label("Browse Catalog")
                        .url("/courses")
                        .variant("link")
                        .type("link")
                        .build())
                .build();

        return ComponentResponse.builder()
                .type("CARD_GRID")
                .config(Map.of("theme", "light", "layout", "grid-3-col"))
                .details(details)
                .build();
    }

    private CardSection toCardSection(ProductListResponse product) {
        String badge = product.getCategories() != null && !product.getCategories().isEmpty()
                ? product.getCategories().get(0).getName()
                : null;

        return CardSection.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getShortDescription())
                .badge(badge)
                .image(ImageResponse.builder()
                        .src(product.getThumbnailUrl())
                        .alt(product.getTitle())
                        .build())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .priceDetails(ProductPriceDetailsFactory.forCatalogSurface(
                        product.getPrice(), product.getDiscountPrice(), currencyFormatter))
                .ctas(List.of(CtaResponse.builder()
                        .label("Enroll Now")
                        .url("/course/" + product.getId())
                        .variant("primary")
                        .type("button")
                        .action("native")
                        .build()))
                .build();
    }

}
