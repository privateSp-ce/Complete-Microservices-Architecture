package com.foodexpress.payment.controller;

import com.foodexpress.payment.dto.PaymentRequest;
import com.foodexpress.payment.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Slf4j
public class PaymentController {

    @PostMapping
    public PaymentResponse processPayment(@RequestBody PaymentRequest request) {
        log.info("Processing payment for Order ID: {} with Amount: {}", request.getOrderId(), request.getAmount());

        // Simulate payment logic
        if (request.getAmount() <= 0) {
            return PaymentResponse.builder()
                    .transactionId(null)
                    .status("FAILED")
                    .message("Invalid amount")
                    .build();
        }

        // Simulate random failure (optional, but good for sad path testing)
        // For now, let's keep it deterministic: if amount is 999, it fails.
        if (request.getAmount() == 999.0) {
            return PaymentResponse.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .status("FAILED")
                    .message("Payment declined by bank")
                    .build();
        }

        return PaymentResponse.builder()
                .transactionId(UUID.randomUUID().toString())
                .status("SUCCESS")
                .message("Payment processed successfully")
                .build();
    }
}
