package com.quantum.edu.bff.service;

import com.quantum.edu.bff.dto.BffCheckoutRequest;
import com.quantum.edu.cart.api.CartApi;
import com.quantum.edu.cart.dto.BillingRequest;
import com.quantum.edu.cart.dto.CheckoutRequest;
import com.quantum.edu.cart.dto.CheckoutResponse;
import com.quantum.edu.common.exception.ApiErrorCode;
import com.quantum.edu.common.exception.ApiException;
import com.quantum.edu.usermgmt.api.UserProfileApi;
import com.quantum.edu.usermgmt.domain.UserProfile;
import org.springframework.stereotype.Service;

@Service
public class BffCheckoutService {

    private final CartApi cartApi;
    private final UserProfileApi userProfileApi;

    public BffCheckoutService(CartApi cartApi, UserProfileApi userProfileApi) {
        this.cartApi = cartApi;
        this.userProfileApi = userProfileApi;
    }

    public CheckoutResponse checkout(Long userId, BffCheckoutRequest request) {
        if (!request.isBillingInfoPresent()) {
            BillingRequest billingFromRequest = request.getBilling();
            userProfileApi.updateBillingInfo(
                    userId,
                    billingFromRequest.getBillingName(),
                    billingFromRequest.getBillingAddressLine1(),
                    billingFromRequest.getBillingAddressLine2(),
                    billingFromRequest.getBillingCity(),
                    billingFromRequest.getBillingState(),
                    billingFromRequest.getBillingCountry(),
                    billingFromRequest.getBillingPostalCode(),
                    billingFromRequest.getBillingGstNumber()
            );
        }

        BillingRequest billingForCart = resolveBillingForCart(userId, request);

        CheckoutRequest checkoutRequest = CheckoutRequest.builder()
                .items(request.getItems())
                .billing(billingForCart)
                .build();

        return cartApi.checkout(userId, checkoutRequest);
    }

    private BillingRequest resolveBillingForCart(Long userId, BffCheckoutRequest request) {
        if (request.getBilling() != null) {
            return request.getBilling();
        }
        if (!request.isBillingInfoPresent()) {
            throw new ApiException(ApiErrorCode.VALIDATION_FAILED);
        }
        UserProfile profile = userProfileApi.getProfile(userId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_PROFILE_NOT_FOUND));
        if (!profile.hasBillingInfo()) {
            throw new ApiException(ApiErrorCode.BFF_BILLING_INCOMPLETE_ON_PROFILE);
        }
        return BillingRequest.builder()
                .billingName(profile.getBillingName())
                .billingAddressLine1(profile.getAddressLine1())
                .billingAddressLine2(blankToNull(profile.getAddressLine2()))
                .billingCity(profile.getCity())
                .billingState(profile.getState())
                .billingCountry(profile.getCountry())
                .billingPostalCode(profile.getPostalCode())
                .billingGstNumber(blankToNull(profile.getGstNumber()))
                .build();
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s;
    }
}
