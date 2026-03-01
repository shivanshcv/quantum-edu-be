package com.quantum.edu.cart.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders",
        indexes = {
                @Index(name = "idx_orders_user_id", columnList = "user_id"),
                @Index(name = "idx_orders_status", columnList = "status"),
                @Index(name = "idx_orders_pg_order_id", columnList = "payment_gateway_order_id")
        })
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "gst_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal gstAmount;

    @Column(name = "billing_name", nullable = false, length = 255)
    private String billingName;

    @Column(name = "billing_address_line1", nullable = false, length = 255)
    private String billingAddressLine1;

    @Column(name = "billing_address_line2", length = 255)
    private String billingAddressLine2;

    @Column(name = "billing_city", nullable = false, length = 100)
    private String billingCity;

    @Column(name = "billing_state", nullable = false, length = 100)
    private String billingState;

    @Column(name = "billing_country", nullable = false, length = 100)
    private String billingCountry;

    @Column(name = "billing_postal_code", nullable = false, length = 20)
    private String billingPostalCode;

    @Column(name = "billing_gst_number", length = 30)
    private String billingGstNumber;

    @Column(name = "payment_gateway_order_id", length = 255)
    private String paymentGatewayOrderId;

    @Column(name = "payment_gateway_payment_id", length = 255)
    private String paymentGatewayPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6)")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "datetime(6) default current_timestamp(6) on update current_timestamp(6)")
    private Instant updatedAt;

    protected Order() {
    }

    public Order(Long userId, Long productId, BigDecimal price, BigDecimal discountPrice,
                 BigDecimal finalPrice, BigDecimal gstAmount, BillingInfo billing) {
        this.userId = userId;
        this.productId = productId;
        this.price = price;
        this.discountPrice = discountPrice;
        this.finalPrice = finalPrice;
        this.gstAmount = gstAmount;
        this.billingName = billing.name;
        this.billingAddressLine1 = billing.addressLine1;
        this.billingAddressLine2 = billing.addressLine2;
        this.billingCity = billing.city;
        this.billingState = billing.state;
        this.billingCountry = billing.country;
        this.billingPostalCode = billing.postalCode;
        this.billingGstNumber = billing.gstNumber;
        this.status = OrderStatus.INITIATED;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getProductId() {
        return productId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getDiscountPrice() {
        return discountPrice;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public BigDecimal getGstAmount() {
        return gstAmount;
    }

    public String getPaymentGatewayOrderId() {
        return paymentGatewayOrderId;
    }

    public void setPaymentGatewayOrderId(String paymentGatewayOrderId) {
        this.paymentGatewayOrderId = paymentGatewayOrderId;
    }

    public String getPaymentGatewayPaymentId() {
        return paymentGatewayPaymentId;
    }

    public void setPaymentGatewayPaymentId(String paymentGatewayPaymentId) {
        this.paymentGatewayPaymentId = paymentGatewayPaymentId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public enum OrderStatus {
        INITIATED,  // Order created, not yet sent to payment gateway
        PENDING,    // Payment pending at gateway
        SUCCESS,    // Payment success, order complete
        FAILED      // Payment failed, order failed
    }

    public record BillingInfo(String name, String addressLine1, String addressLine2,
                              String city, String state, String country, String postalCode, String gstNumber) {
    }
}
