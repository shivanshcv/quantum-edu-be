package com.quantum.edu.cart.service;

import com.quantum.edu.cart.domain.Order;
import com.quantum.edu.cart.dto.CheckoutItemRequest;
import com.quantum.edu.cart.dto.CheckoutRequest;
import com.quantum.edu.cart.dto.CheckoutResponse;
import com.quantum.edu.cart.repository.CartRepository;
import com.quantum.edu.cart.repository.OrderRepository;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import com.quantum.edu.common.util.CurrencyFormatter;
import com.quantum.edu.ownership.api.OwnershipApi;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final String FREE_PG_ORDER_ID = "FREE";

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OwnershipApi ownershipApi;
    private final PaymentService paymentService;
    private final CurrencyFormatter currencyFormatter;
    private final OrderService self;

    public OrderService(CartRepository cartRepository, OrderRepository orderRepository,
                        OwnershipApi ownershipApi, PaymentService paymentService,
                        CurrencyFormatter currencyFormatter, @Lazy OrderService self) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.ownershipApi = ownershipApi;
        this.paymentService = paymentService;
        this.currencyFormatter = currencyFormatter;
        this.self = self;
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
        List<CheckoutItemRequest> freeItems = new ArrayList<>();
        List<CheckoutItemRequest> paidItems = new ArrayList<>();

        for (var item : request.getItems()) {
            if (!cartSet.contains(item.getProductId())) {
                continue;
            }
            if (ownershipApi.ownsCourse(userId, item.getProductId())) {
                throw new InternalException(InternalErrorCode.CART_ITEMS_ALREADY_OWNED);
            }
            boolean isFree = item.isFree() || item.getFinalPrice().compareTo(BigDecimal.ZERO) == 0;
            if (isFree) {
                freeItems.add(item);
            } else {
                paidItems.add(item);
            }
        }

        if (freeItems.isEmpty() && paidItems.isEmpty()) {
            throw new InternalException(InternalErrorCode.CART_CHECKOUT_ITEMS_MISMATCH);
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

        CompletableFuture<List<Long>> freeOrderIdsFuture = CompletableFuture.supplyAsync(
                () -> self.processFreeItems(userId, freeItems, billing));
        CompletableFuture<PaidResult> paidResultFuture = CompletableFuture.supplyAsync(
                () -> self.processPaidItems(userId, paidItems, billing));

        List<Long> freeOrderIds = freeOrderIdsFuture.join();
        PaidResult paidResult = paidResultFuture.join();

        boolean paymentRequired = !paidResult.orderIds().isEmpty();

        return CheckoutResponse.builder()
                .paymentRequired(paymentRequired)
                .razorpayOrderId(paymentRequired ? paidResult.razorpayOrderId() : null)
                .amount(paymentRequired ? paidResult.amount() : null)
                .currency(paymentRequired ? paidResult.currency() : null)
                .keyId(paymentRequired ? paymentService.getKeyId() : null)
                .orderIds(paidResult.orderIds())
                .freeOrderIds(freeOrderIds)
                .build();
    }

    @Transactional
    public List<Long> processFreeItems(Long userId, List<CheckoutItemRequest> items, Order.BillingInfo billing) {
        if (items.isEmpty()) {
            return List.of();
        }
        List<Long> orderIds = new ArrayList<>();
        for (var item : items) {
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
            order.setPaymentGatewayOrderId(FREE_PG_ORDER_ID);
            order.setStatus(Order.OrderStatus.SUCCESS);
            orderRepository.save(order);
            ownershipApi.createOwnership(userId, item.getProductId(), order.getId());
            cartRepository.deleteByUserIdAndProductId(userId, item.getProductId());
            orderIds.add(order.getId());
        }
        return orderIds;
    }

    @Transactional
    public PaidResult processPaidItems(Long userId, List<CheckoutItemRequest> items, Order.BillingInfo billing) {
        if (items.isEmpty()) {
            return new PaidResult(List.of(), null, 0L, null);
        }
        List<Order> orders = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (var item : items) {
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
        long amountPaise = totalAmount.multiply(BigDecimal.valueOf(100)).longValue();
        var razorpayOrder = paymentService.createOrder(amountPaise, currencyFormatter.getCurrencyCode());
        String pgOrderId = razorpayOrder.orderId();
        for (Order order : orders) {
            order.setPaymentGatewayOrderId(pgOrderId);
            order.setStatus(Order.OrderStatus.PENDING);
        }
        orderRepository.saveAll(orders);
        return new PaidResult(
                orders.stream().map(Order::getId).collect(Collectors.toList()),
                pgOrderId,
                razorpayOrder.amount(),
                razorpayOrder.currency()
        );
    }

    private record PaidResult(List<Long> orderIds, String razorpayOrderId, long amount, String currency) {
    }
}
