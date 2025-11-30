package com.foodexpress.order.controller;

import com.foodexpress.order.dto.OrderRequest;
import com.foodexpress.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
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
        String orderTrackingId = orderService.placeOrder(userId, orderRequest);
        return ResponseEntity.ok("Order placed successfully! Tracking ID: " + orderTrackingId);
    }

    @PostMapping("/{orderTrackingNumber}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderTrackingNumber) {
        orderService.cancelOrder(orderTrackingNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderTrackingNumber}/prepare")
    public ResponseEntity<Void> startPreparation(@PathVariable String orderTrackingNumber) {
        orderService.startPreparation(orderTrackingNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderTrackingNumber}/ready")
    public ResponseEntity<Void> readyForPickup(@PathVariable String orderTrackingNumber) {
        orderService.readyForPickup(orderTrackingNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderTrackingNumber}/deliver")
    public ResponseEntity<Void> startDelivery(@PathVariable String orderTrackingNumber) {
        orderService.startDelivery(orderTrackingNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderTrackingNumber}/complete")
    public ResponseEntity<Void> completeDelivery(@PathVariable String orderTrackingNumber) {
        orderService.completeDelivery(orderTrackingNumber);
        return ResponseEntity.ok().build();
    }
}
