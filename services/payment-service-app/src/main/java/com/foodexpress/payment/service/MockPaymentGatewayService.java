package com.foodexpress.payment.service;

import com.foodexpress.payment.dto.PaymentRequest;
import com.foodexpress.payment.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class MockPaymentGatewayService implements PaymentGatewayService {

    @Override
    public PaymentResponse process(PaymentRequest request) {
        log.info("Mock Gateway processing payment for Order ID: {}", request.getOrderId());

        // Simulate network delay
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Logic (can be expanded)
        if (request.getAmount() <= 0) {
            return PaymentResponse.builder()
                    .status("FAILED")
                    .message("Invalid amount")
                    .build();
        }

        if (request.getAmount() == 999.0) {
            return PaymentResponse.builder()
                    .status("FAILED")
                    .message("Insufficient funds (Mock)")
                    .build();
        }

        return PaymentResponse.builder()
                .transactionId(UUID.randomUUID().toString())
                .status("SUCCESS")
                .message("Payment approved")
                .build();
    }
}
