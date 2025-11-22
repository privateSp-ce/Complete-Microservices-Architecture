package com.foodexpress.order.service;

import com.foodexpress.order.client.CartServiceClient;
import com.foodexpress.order.dto.CartResponse;
import com.foodexpress.order.dto.OrderRequest;
import com.foodexpress.order.entity.Order;
import com.foodexpress.order.entity.OrderItem;
import com.foodexpress.order.enums.OrderStatus;
import com.foodexpress.order.enums.PaymentStatus;
import com.foodexpress.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartServiceClient cartServiceClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public String placeOrder(String userId, OrderRequest request) {

        // 1. Get Cart Details from Cart Service (Microservice Communication)
        CartResponse cart = cartServiceClient.getCart(userId);

        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty! Cannot place order.");
        }

        // 2. Convert Cart to Order Entity
        Order order = Order.builder()
                .userId(Long.parseLong(userId)) // Assuming User ID is Long
                .restaurantId(cart.getRestaurantId())
                .restaurantName(cart.getRestaurantName())
                .orderTrackingNumber(UUID.randomUUID().toString())
                .status(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .deliveryAddress(request.getDeliveryAddress())
                .build();

        // Map Cart Items to Order Items
        order.setOrderItems(cart.getItems().stream().map(cartItem ->
                OrderItem.builder()
                        .order(order)
                        .menuItemId(cartItem.getMenuItemId()) // Now String, correct?
                        .name(cartItem.getItemName())
                        .price(cartItem.getPrice())
                        .quantity(cartItem.getQuantity())
                        .subtotal(cartItem.getSubtotal())
                        .imageUrl(cartItem.getImageUrl())
                        .build()
        ).collect(Collectors.toList()));

        // 3. Save Order to MySQL
        orderRepository.save(order);
        log.info("Order saved with ID: {}", order.getId());

        // 4. Send Notification via Kafka (Async)
        String message = "Order Placed! ID: " + order.getOrderTrackingNumber() + " for ₹" + order.getTotalAmount();
        kafkaTemplate.send("order-notifications", message);

        // 5. Clear Cart (Cleanup)
        cartServiceClient.clearCart(userId);

        return order.getOrderTrackingNumber();
    }
}