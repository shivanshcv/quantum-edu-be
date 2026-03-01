package com.quantum.edu.cart.service;

import com.quantum.edu.cart.domain.Order;
import com.quantum.edu.cart.dto.CheckoutRequest;
import com.quantum.edu.cart.dto.CheckoutResponse;
import com.quantum.edu.cart.repository.CartRepository;
import com.quantum.edu.cart.repository.OrderRepository;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import com.quantum.edu.ownership.api.OwnershipApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final String CURRENCY = "INR";

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OwnershipApi ownershipApi;
    private final PaymentService paymentService;

    public OrderService(CartRepository cartRepository, OrderRepository orderRepository,
                        OwnershipApi ownershipApi, PaymentService paymentService) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.ownershipApi = ownershipApi;
        this.paymentService = paymentService;
    }

    @Transactional
    public CheckoutResponse checkout(Long userId, CheckoutRequest request) {
        List<Long> cartProductIds = cartRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(c -> c.getProductId())
                .collect(Collectors.toList());

        if (cartProductIds.isEmpty()) {
            throw new InternalException(InternalErrorCode.CART_EMPTY);
        }

        Set<Long> cartSet = Set.copyOf(cartProductIds);
        List<Order> orders = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var item : request.getItems()) {
            if (!cartSet.contains(item.getProductId())) {
                continue;
            }
            if (ownershipApi.ownsCourse(userId, item.getProductId())) {
                throw new InternalException(InternalErrorCode.CART_ITEMS_ALREADY_OWNED);
            }
            var billing = new Order.BillingInfo(
                    request.getBilling().getBillingName(),
                    request.getBilling().getBillingAddressLine1(),
                    request.getBilling().getBillingAddressLine2(),
                    request.getBilling().getBillingCity(),
                    request.getBilling().getBillingState(),
                    request.getBilling().getBillingCountry(),
                    request.getBilling().getBillingPostalCode(),
                    request.getBilling().getBillingGstNumber()
            );
            Order order = new Order(
                    userId,
                    item.getProductId(),
                    item.getPrice(),
                    item.getDiscountPrice(),
                    item.getFinalPrice(),
                    item.getGstAmount(),
                    billing
            );
            order = orderRepository.save(order);
            orders.add(order);
            totalAmount = totalAmount.add(item.getFinalPrice()).add(item.getGstAmount());
        }

        if (orders.isEmpty()) {
            throw new InternalException(InternalErrorCode.CART_CHECKOUT_ITEMS_MISMATCH);
        }

        long amountPaise = totalAmount.multiply(BigDecimal.valueOf(100)).longValue();
        var razorpayOrder = paymentService.createOrder(amountPaise, CURRENCY);

        String pgOrderId = razorpayOrder.orderId();
        for (Order order : orders) {
            order.setPaymentGatewayOrderId(pgOrderId);
            order.setStatus(Order.OrderStatus.PENDING);
        }
        orderRepository.saveAll(orders);

        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        return CheckoutResponse.builder()
                .razorpayOrderId(pgOrderId)
                .amount(razorpayOrder.amount())
                .currency(razorpayOrder.currency())
                .keyId(paymentService.getKeyId())
                .orderIds(orderIds)
                .build();
    }
}
