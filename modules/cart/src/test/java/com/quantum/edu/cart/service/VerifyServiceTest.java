package com.quantum.edu.cart.service;

import com.quantum.edu.cart.domain.CartItem;
import com.quantum.edu.cart.dto.VerifyItemRequest;
import com.quantum.edu.cart.dto.VerifyRequest;
import com.quantum.edu.cart.repository.CartRepository;
import com.quantum.edu.common.config.CurrencyProperties;
import com.quantum.edu.common.util.CurrencyFormatter;
import com.quantum.edu.ownership.api.OwnershipApi;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VerifyServiceTest {

    @Test
    void verify_finalAmountEqualsSubtotal_taxInclusiveNoExtraLine() {
        CartRepository cartRepo = mock(CartRepository.class);
        when(cartRepo.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(new CartItem(1L, 10L)));

        OwnershipApi ownership = mock(OwnershipApi.class);
        when(ownership.ownsCourse(1L, 10L)).thenReturn(false);

        CurrencyFormatter fmt = new CurrencyFormatter(new CurrencyProperties());
        VerifyService svc = new VerifyService(cartRepo, ownership, fmt);

        VerifyRequest req = VerifyRequest.builder()
                .items(List.of(VerifyItemRequest.builder()
                        .productId(10L)
                        .title("Course")
                        .price(new BigDecimal("1000.00"))
                        .discountPrice(new BigDecimal("799.00"))
                        .free(false)
                        .build()))
                .build();

        var resp = svc.verify(1L, req);

        assertEquals(new BigDecimal("799.00"), resp.getSubtotal());
        assertEquals(new BigDecimal("799.00"), resp.getFinalAmount());
        assertEquals(1, resp.getItems().size());
        assertEquals(new BigDecimal("799.00"), resp.getItems().get(0).getFinalPrice());
        assertTrue(resp.isPaymentRequired());
    }
}
