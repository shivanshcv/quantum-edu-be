package com.quantum.edu.cart.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    private static final String RAZORPAY_ORDERS_URL = "https://api.razorpay.com/v1/orders";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${razorpay.key_id:}")
    private String keyId;

    @Value("${razorpay.key_secret:}")
    private String keySecret;

    public RazorpayOrderResponse createOrder(long amountPaise, String currency) {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amountPaise);
        body.put("currency", currency);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + Base64.getEncoder()
                .encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8)));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                RAZORPAY_ORDERS_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                JsonNode node = objectMapper.readTree(response.getBody());
                return new RazorpayOrderResponse(
                        node.get("id").asText(),
                        node.get("amount").asLong(),
                        node.get("currency").asText()
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse Razorpay response", e);
            }
        }
        throw new RuntimeException("Failed to create Razorpay order: " + response.getStatusCode());
    }

    public String getKeyId() {
        return keyId;
    }

    public record RazorpayOrderResponse(String orderId, long amount, String currency) {
    }
}
