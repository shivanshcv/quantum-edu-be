package com.quantum.edu.cart.service;

import com.quantum.edu.cart.domain.Order;
import com.quantum.edu.cart.repository.CartRepository;
import com.quantum.edu.cart.repository.OrderRepository;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import com.quantum.edu.ownership.api.OwnershipApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class WebhookService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final OwnershipApi ownershipApi;

    public WebhookService(OrderRepository orderRepository, CartRepository cartRepository,
                          OwnershipApi ownershipApi) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.ownershipApi = ownershipApi;
    }

    @Transactional
    public void processPaymentSuccess(String razorpayOrderId, String razorpayPaymentId) {
        List<Order> orders = orderRepository.findByPaymentGatewayOrderIdForUpdate(razorpayOrderId);
        if (orders.isEmpty()) {
            return;
        }
        Instant purchasedAt = Instant.now();
        for (Order order : orders) {
            if (order.getStatus() == Order.OrderStatus.SUCCESS) {
                continue;
            }
            order.setStatus(Order.OrderStatus.SUCCESS);
            order.setPaymentGatewayPaymentId(razorpayPaymentId);
            orderRepository.save(order);
            ownershipApi.createOwnership(order.getUserId(), order.getProductId(), order.getId());
            cartRepository.deleteByUserIdAndProductId(order.getUserId(), order.getProductId());
        }
    }
}
