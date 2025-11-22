package com.foodexpress.order.controller;

import com.foodexpress.order.dto.OrderRequest;
import com.foodexpress.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/orders") -> no need of this as we are doing this from api gateway itself
//@CrossOrigin(origins = "*") -> no need as we already configured global config cors
@RequestMapping("/api/v1/orders")
@RefreshScope
@Slf4j
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/place")
    public ResponseEntity<String> placeOrder(
            @RequestHeader("X-User-Id") String userId, // Gateway nunchi user ID
            @RequestBody OrderRequest orderRequest
    ) {
        log.info("Placing order for User ID: {}", userId);

        // Call Business Logic
        String orderTrackingId = orderService.placeOrder(userId, orderRequest);

        return ResponseEntity.ok("Order placed successfully! Tracking ID: " + orderTrackingId);
    }

}
