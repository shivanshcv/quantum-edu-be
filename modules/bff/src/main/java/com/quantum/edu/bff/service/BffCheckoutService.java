package com.quantum.edu.bff.service;

import com.quantum.edu.bff.dto.BffCheckoutRequest;
import com.quantum.edu.cart.api.CartApi;
import com.quantum.edu.cart.dto.BillingRequest;
import com.quantum.edu.cart.dto.CheckoutRequest;
import com.quantum.edu.cart.dto.CheckoutResponse;
import com.quantum.edu.usermgmt.api.UserProfileApi;
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
            BillingRequest billing = request.getBilling();
            userProfileApi.updateBillingInfo(
                    userId,
                    billing.getBillingName(),
                    billing.getBillingAddressLine1(),
                    billing.getBillingAddressLine2(),
                    billing.getBillingCity(),
                    billing.getBillingState(),
                    billing.getBillingCountry(),
                    billing.getBillingPostalCode(),
                    billing.getBillingGstNumber()
            );
        }

        CheckoutRequest checkoutRequest = CheckoutRequest.builder()
                .items(request.getItems())
                .billing(request.getBilling())
                .build();

        return cartApi.checkout(userId, checkoutRequest);
    }
}
