package com.foodexpress.payment.controller;

import com.foodexpress.payment.dto.PaymentRequest;
import com.foodexpress.payment.dto.PaymentResponse;
import com.foodexpress.payment.service.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentGatewayService paymentGatewayService;

    @PostMapping
    public PaymentResponse processPayment(@RequestBody PaymentRequest request) {
        log.info("Processing payment for Order ID: {} with Amount: {}", request.getOrderId(), request.getAmount());
        return paymentGatewayService.process(request);
    }
}
