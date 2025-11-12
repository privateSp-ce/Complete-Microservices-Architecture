package com.spring_boot.payment_service_app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    @GetMapping("/payments")
    public String getPaymentMessage() {
        return "Payment Service: Payment processed successfully! 💳";
    }

}
