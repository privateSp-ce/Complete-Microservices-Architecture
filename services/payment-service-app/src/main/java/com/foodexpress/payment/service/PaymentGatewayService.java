package com.foodexpress.payment.service;

import com.foodexpress.payment.dto.PaymentRequest;
import com.foodexpress.payment.dto.PaymentResponse;

public interface PaymentGatewayService {
    PaymentResponse process(PaymentRequest request);
}
