package com.quantum.edu.bff.service;

import com.quantum.edu.bff.dto.*;
import com.quantum.edu.cart.api.CartApi;
import com.quantum.edu.cart.dto.VerifyItemRequest;
import com.quantum.edu.cart.dto.VerifyRequest;
import com.quantum.edu.catalogue.api.ProductCatalogueApi;
import com.quantum.edu.catalogue.dto.ProductDetailResponse;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import com.quantum.edu.usermgmt.api.UserProfileApi;
import com.quantum.edu.usermgmt.domain.UserProfile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VerifyCartPageService {

    private static final List<VerifyCartBillingDetails.BillingField> BILLING_FIELDS = List.of(
            VerifyCartBillingDetails.BillingField.builder()
                    .id("billingName").label("Full name").type("text").placeholder("Jane Doe").required(true).build(),
            VerifyCartBillingDetails.BillingField.builder()
                    .id("billingAddressLine1").label("Address line 1").type("text").placeholder("123 Main Street").required(true).build(),
            VerifyCartBillingDetails.BillingField.builder()
                    .id("billingAddressLine2").label("Address line 2 (optional)").type("text").placeholder("Apt 4B").required(false).build(),
            VerifyCartBillingDetails.BillingField.builder()
                    .id("billingCity").label("City").type("text").placeholder("Mumbai").required(true).build(),
            VerifyCartBillingDetails.BillingField.builder()
                    .id("billingState").label("State").type("text").placeholder("Maharashtra").required(true).build(),
            VerifyCartBillingDetails.BillingField.builder()
                    .id("billingCountry").label("Country").type("text").placeholder("India").required(true).build(),
            VerifyCartBillingDetails.BillingField.builder()
                    .id("billingPostalCode").label("Postal code").type("text").placeholder("400001").required(true).build(),
            VerifyCartBillingDetails.BillingField.builder()
                    .id("billingGstNumber").label("GST number (optional)").type("text").placeholder("22AAAAA0000A1Z5").required(false).build()
    );

    private final CartApi cartApi;
    private final ProductCatalogueApi productCatalogueApi;
    private final UserProfileApi userProfileApi;

    public VerifyCartPageService(CartApi cartApi, ProductCatalogueApi productCatalogueApi,
                                 UserProfileApi userProfileApi) {
        this.cartApi = cartApi;
        this.productCatalogueApi = productCatalogueApi;
        this.userProfileApi = userProfileApi;
    }

    public PageResponse getVerifyCartPage(Long userId, boolean isVerified, BffVerifyCartRequest request) {
        Optional<UserProfile> profileOpt = userProfileApi.getProfile(userId);
        UserProfile profile = profileOpt.orElse(null);

        boolean billingAvailable = profile != null && profile.hasBillingInfo();
        String billingGstNumber = (profile != null && profile.getGstNumber() != null) ? profile.getGstNumber() : null;

        var cartItems = request.getItems();
        if (cartItems == null || cartItems.isEmpty()) {
            return buildEmptyVerifyPage(billingAvailable, profile, isVerified);
        }

        List<VerifyItemRequest> verifyItems = new ArrayList<>();
        for (var cartItem : cartItems) {
            ProductDetailResponse product = productCatalogueApi.getPublishedProductById(cartItem.getProductId());
            BigDecimal expectedFinalPrice = product.getDiscountPrice() != null
                    && product.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0
                    ? product.getDiscountPrice()
                    : product.getPrice();
            BigDecimal fePrice = cartItem.getPrice() != null ? cartItem.getPrice().setScale(2, RoundingMode.HALF_UP) : null;
            if (fePrice == null || fePrice.compareTo(expectedFinalPrice.setScale(2, RoundingMode.HALF_UP)) != 0) {
                throw new InternalException(InternalErrorCode.PRICE_MISMATCH);
            }
            verifyItems.add(VerifyItemRequest.builder()
                    .productId(product.getId())
                    .title(product.getTitle())
                    .price(product.getPrice())
                    .discountPrice(product.getDiscountPrice())
                    .build());
        }

        VerifyRequest verifyRequest = VerifyRequest.builder()
                .items(verifyItems)
                .billingGstNumber(billingGstNumber)
                .build();

        var verifyResponse = cartApi.verify(userId, verifyRequest);

        return buildVerifyPage(verifyResponse, billingAvailable, profile, isVerified);
    }

    private PageResponse buildEmptyVerifyPage(boolean billingAvailable, UserProfile profile, boolean isVerified) {
        VerifyCartDetails details = VerifyCartDetails.builder()
                .badge("CHECKOUT")
                .billingDetails(buildBillingDetails(billingAvailable, profile))
                .emailVerification(buildEmailVerification(isVerified))
                .title("Checkout")
                .subtitle("Review your courses and continue to payment.")
                .courseListTitle("Course List")
                .orderSummaryTitle("Order Summary")
                .subtotalLabel("Subtotal")
                .taxesLabel("Taxes (if applicable)")
                .totalLabel("Final Payable Total")
                .payNowLabel("Pay now")
                .emptyStateMessage("No courses in your cart yet.")
                .loadingMessage("Loading checkout details...")
                .errorMessage("Unable to load checkout details. Please try again.")
                .items(List.of())
                .subtotal("INR 0")
                .taxes("INR 0")
                .total("INR 0")
                .build();

        return buildPageResponse(details);
    }

    private PageResponse buildVerifyPage(com.quantum.edu.cart.dto.VerifyResponse verifyResponse,
                                         boolean billingAvailable, UserProfile profile, boolean isVerified) {
        List<VerifyCartItemDto> items = new ArrayList<>();
        if (verifyResponse.getItems() != null) {
            for (var item : verifyResponse.getItems()) {
                items.add(VerifyCartItemDto.builder()
                        .productId(item.getProductId())
                        .title(item.getTitle())
                        .price(item.getPrice())
                        .discountPrice(item.getDiscountPrice())
                        .finalPrice(item.getFinalPrice())
                        .gstAmount(item.getGstAmount())
                        .quantity(item.getQuantity())
                        .build());
            }
        }

        String subtotal = formatCurrency(verifyResponse.getSubtotal());
        String taxes = formatCurrency(verifyResponse.getGstAmount());
        String total = formatCurrency(verifyResponse.getFinalAmount());

        VerifyCartDetails details = VerifyCartDetails.builder()
                .badge("CHECKOUT")
                .billingDetails(buildBillingDetails(billingAvailable, profile))
                .emailVerification(buildEmailVerification(isVerified))
                .title("Checkout")
                .subtitle("Review your courses and continue to payment.")
                .courseListTitle("Course List")
                .orderSummaryTitle("Order Summary")
                .subtotalLabel("Subtotal")
                .taxesLabel("Taxes (if applicable)")
                .totalLabel("Final Payable Total")
                .payNowLabel("Pay now")
                .emptyStateMessage("No courses in your cart yet.")
                .loadingMessage("Loading checkout details...")
                .errorMessage("Unable to load checkout details. Please try again.")
                .items(items)
                .subtotal(subtotal)
                .taxes(taxes)
                .total(total)
                .build();

        return buildPageResponse(details);
    }

    private VerifyCartBillingDetails buildBillingDetails(boolean isAvailable, UserProfile profile) {
        if (isAvailable && profile != null) {
            VerifyCartBillingDetails.BillingData billing = VerifyCartBillingDetails.BillingData.builder()
                    .billingName(profile.getBillingName())
                    .billingAddressLine1(profile.getAddressLine1())
                    .billingAddressLine2(profile.getAddressLine2())
                    .billingCity(profile.getCity())
                    .billingState(profile.getState())
                    .billingCountry(profile.getCountry())
                    .billingPostalCode(profile.getPostalCode())
                    .billingGstNumber(profile.getGstNumber())
                    .build();
            return VerifyCartBillingDetails.builder()
                    .isAvailable(true)
                    .title("Billing details")
                    .billing(billing)
                    .build();
        }
        VerifyCartBillingDetails.BillingSection section = VerifyCartBillingDetails.BillingSection.builder()
                .fields(BILLING_FIELDS)
                .submitLabel("Save changes")
                .build();
        return VerifyCartBillingDetails.builder()
                .isAvailable(false)
                .title("Billing details")
                .section(section)
                .build();
    }

    private VerifyCartEmailVerification buildEmailVerification(boolean isVerified) {
        return VerifyCartEmailVerification.builder()
                .isVerified(isVerified)
                .verificationMessage(isVerified ? null : "Please verify your email to continue.")
                .verificationButtonText(isVerified ? null : "Verify Email")
                .verificationButtonLink(isVerified ? null : "/verify-email")
                .build();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "INR 0";
        return "INR " + amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private PageResponse buildPageResponse(VerifyCartDetails details) {
        ComponentResponse component = ComponentResponse.builder()
                .type("VERIFY_CART")
                .config(java.util.Map.of("theme", "light"))
                .details(details)
                .build();

        MainSection main = MainSection.builder()
                .type("VERIFY_CART")
                .components(List.of(component))
                .data(java.util.Map.of())
                .build();

        return PageResponse.builder()
                .main(main)
                .build();
    }
}
