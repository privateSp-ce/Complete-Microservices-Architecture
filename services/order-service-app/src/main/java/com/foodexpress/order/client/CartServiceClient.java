package com.foodexpress.order.client;

import com.foodexpress.order.dto.CartResponse; // We need to map Cart Response here
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "CART-SERVICE-APP") // Eureka ID matches cart-service-app
public interface CartServiceClient {

    // Cart details techukodaniki
    @GetMapping("/api/v1/cart")
    CartResponse getCart(@RequestHeader("X-User-Id") String userId);

    // Order aipoyaka cart clear cheyadaniki
    @DeleteMapping("/api/v1/cart")
    void clearCart(@RequestHeader("X-User-Id") String userId);
}