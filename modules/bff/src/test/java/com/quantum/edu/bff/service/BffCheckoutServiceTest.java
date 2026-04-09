package com.quantum.edu.bff.service;

import com.quantum.edu.bff.dto.BffCheckoutRequest;
import com.quantum.edu.cart.api.CartApi;
import com.quantum.edu.cart.dto.BillingRequest;
import com.quantum.edu.cart.dto.CheckoutItemRequest;
import com.quantum.edu.cart.dto.CheckoutResponse;
import com.quantum.edu.common.exception.ApiErrorCode;
import com.quantum.edu.common.exception.ApiException;
import com.quantum.edu.usermgmt.api.UserProfileApi;
import com.quantum.edu.usermgmt.domain.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BffCheckoutServiceTest {

    private CartApi cartApi;
    private UserProfileApi userProfileApi;
    private BffCheckoutService service;

    @BeforeEach
    void setUp() {
        cartApi = mock(CartApi.class);
        userProfileApi = mock(UserProfileApi.class);
        service = new BffCheckoutService(cartApi, userProfileApi);
    }

    private static List<CheckoutItemRequest> sampleItems() {
        return List.of(CheckoutItemRequest.builder()
                .productId(1L)
                .price(new BigDecimal("100"))
                .finalPrice(new BigDecimal("100"))
                .build());
    }

    @Test
    void checkout_whenBillingInfoPresentAndBillingNull_loadsFromProfile() {
        UserProfile profile = new UserProfile(1L, "Jane", "Doe", null);
        profile.setBillingName("Jane Doe");
        profile.setAddressLine1("123 St");
        profile.setCity("Mumbai");
        profile.setState("MH");
        profile.setCountry("India");
        profile.setPostalCode("400001");

        when(userProfileApi.getProfile(10L)).thenReturn(Optional.of(profile));
        when(cartApi.checkout(eq(10L), any())).thenReturn(CheckoutResponse.builder().build());

        BffCheckoutRequest req = BffCheckoutRequest.builder()
                .billingInfoPresent(true)
                .items(sampleItems())
                .billing(null)
                .build();

        service.checkout(10L, req);

        verify(userProfileApi, never()).updateBillingInfo(
                anyLong(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(cartApi).checkout(eq(10L), argThat(cr ->
                "Jane Doe".equals(cr.getBilling().getBillingName())
                        && "123 St".equals(cr.getBilling().getBillingAddressLine1())));
    }

    @Test
    void checkout_whenBillingInfoPresentAndBillingIncompleteOnProfile_throws() {
        UserProfile profile = new UserProfile(1L, "Jane", "Doe", null);
        when(userProfileApi.getProfile(10L)).thenReturn(Optional.of(profile));

        BffCheckoutRequest req = BffCheckoutRequest.builder()
                .billingInfoPresent(true)
                .items(sampleItems())
                .billing(null)
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> service.checkout(10L, req));
        assertEquals(ApiErrorCode.BFF_BILLING_INCOMPLETE_ON_PROFILE, ex.getErrorCode());
    }

    @Test
    void checkout_whenBillingInfoPresentFalse_savesProfileAndUsesRequestBilling() {
        BillingRequest billing = BillingRequest.builder()
                .billingName("A")
                .billingAddressLine1("B")
                .billingCity("C")
                .billingState("D")
                .billingCountry("E")
                .billingPostalCode("F")
                .build();

        when(cartApi.checkout(anyLong(), any())).thenReturn(CheckoutResponse.builder().build());

        BffCheckoutRequest req = BffCheckoutRequest.builder()
                .billingInfoPresent(false)
                .items(sampleItems())
                .billing(billing)
                .build();

        service.checkout(10L, req);

        verify(userProfileApi).updateBillingInfo(10L, "A", "B", null, "C", "D", "E", "F", null);
        verify(cartApi).checkout(eq(10L), argThat(cr -> cr.getBilling() == billing));
    }
}
