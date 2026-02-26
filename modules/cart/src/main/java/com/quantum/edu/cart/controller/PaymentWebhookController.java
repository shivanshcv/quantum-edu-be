package com.quantum.edu.cart.controller;

import com.quantum.edu.cart.service.WebhookService;
import com.quantum.edu.common.dto.ApiResponse;
import com.quantum.edu.common.exception.ApiErrorCode;
import com.quantum.edu.common.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/v1/cart")
public class PaymentWebhookController {

    private static final String PAYMENT_CAPTURED = "payment.captured";

    private final WebhookService webhookService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${razorpay.webhook_secret:}")
    private String webhookSecret;

    public PaymentWebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/webhook/razorpay")
    public ResponseEntity<ApiResponse<Void>> handleRazorpayWebhook(
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature,
            @RequestBody String payload) {
        if (signature == null || signature.isBlank() || webhookSecret == null || webhookSecret.isBlank()) {
            throw new ApiException(ApiErrorCode.CART_INVALID_WEBHOOK_SIGNATURE);
        }
        if (!verifySignature(payload, signature)) {
            throw new ApiException(ApiErrorCode.CART_INVALID_WEBHOOK_SIGNATURE);
        }
        try {
            JsonNode root = objectMapper.readTree(payload);
            String event = root.get("event").asText();
            if (PAYMENT_CAPTURED.equals(event)) {
                JsonNode payment = root.get("payload").get("payment").get("entity");
                String orderId = payment.get("order_id").asText();
                String paymentId = payment.get("id").asText();
                webhookService.processPaymentSuccess(orderId, paymentId);
            }
        } catch (Exception e) {
            throw new ApiException(ApiErrorCode.CART_INVALID_WEBHOOK_SIGNATURE);
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private boolean verifySignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = bytesToHex(hash);
            return signature.equals(expected);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
