package com.foodexpress.order.service;

import com.foodexpress.order.client.CartServiceClient;
import com.foodexpress.order.client.PaymentServiceClient;
import com.foodexpress.order.dto.CartResponse;
import com.foodexpress.order.dto.OrderRequest;
import com.foodexpress.order.dto.PaymentRequest;
import com.foodexpress.order.dto.PaymentResponse;
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
    private final PaymentServiceClient paymentServiceClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // Removing @Transactional from the main method to control transaction boundaries manually
    public String placeOrder(String userId, OrderRequest request) {

        // 1. Create and Save Order (Initial State: CREATED, PENDING)
        Order order = createOrder(userId, request);

        // 2. Process Payment (External Call - Outside Transaction)
        PaymentResponse paymentResponse;
        try {
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .orderId(order.getOrderTrackingNumber())
                    .amount(order.getTotalAmount().doubleValue())
                    .paymentMethod("CARD")
                    .build();

            paymentResponse = paymentServiceClient.processPayment(paymentRequest);
        } catch (Exception e) {
            log.error("Payment failed for Order ID: {}", order.getId(), e);
            updateOrderStatus(order, OrderStatus.CANCELLED, PaymentStatus.FAILED);
            return order.getOrderTrackingNumber();
        }

        // 3. Update Order based on Payment Response
        if ("SUCCESS".equals(paymentResponse.getStatus())) {
            updateOrderStatus(order, OrderStatus.CONFIRMED, PaymentStatus.COMPLETED);

            // 4. Send Notification via Kafka (Async)
            String message = "Order Placed & Paid! ID: " + order.getOrderTrackingNumber() + " for ₹" + order.getTotalAmount();
            kafkaTemplate.send("order-notifications", message);

            // 5. Clear Cart (Cleanup)
            try {
                cartServiceClient.clearCart(userId);
            } catch (Exception e) {
                log.error("Failed to clear cart for User ID: {}", userId, e);
                // We don't fail the order here, but we should log it or retry
            }
        } else {
            updateOrderStatus(order, OrderStatus.CANCELLED, PaymentStatus.FAILED);
        }

        return order.getOrderTrackingNumber();
    }

    @Transactional
    protected Order createOrder(String userId, OrderRequest request) {
        CartResponse cart = cartServiceClient.getCart(userId);

        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty! Cannot place order.");
        }

        Order order = Order.builder()
                .userId(Long.parseLong(userId))
                .restaurantId(cart.getRestaurantId())
                .restaurantName(cart.getRestaurantName())
                .orderTrackingNumber(UUID.randomUUID().toString())
                .status(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .deliveryAddress(request.getDeliveryAddress())
                .build();

        order.setOrderItems(cart.getItems().stream().map(cartItem ->
                OrderItem.builder()
                        .order(order)
                        .menuItemId(cartItem.getMenuItemId())
                        .name(cartItem.getItemName())
                        .price(cartItem.getPrice())
                        .quantity(cartItem.getQuantity())
                        .subtotal(cartItem.getSubtotal())
                        .imageUrl(cartItem.getImageUrl())
                        .build()
        ).collect(Collectors.toList()));

        orderRepository.save(order);
        log.info("Order created with ID: {}", order.getId());
        return order;
    }

    @Transactional
    protected void updateOrderStatus(Order order, OrderStatus status, PaymentStatus paymentStatus) {
        order.setStatus(status);
        order.setPaymentStatus(paymentStatus);
        orderRepository.save(order);
    }
}