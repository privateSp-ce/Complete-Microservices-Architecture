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
import com.foodexpress.order.service.statemachine.OrderStateService;
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
    private final OrderStateService orderStateService; // Integrated State Service

    // We use a self-injected proxy or TransactionTemplate, but for simplicity here and to avoid circular dependencies issues,
    // we will rely on OrderStateService which is a separate bean and can manage its own transactions for status updates.
    // For creation, we will move the logic to a separate helper service or keep it here but we need to ensure transactional behavior.
    // To solve the "this.call()" transaction issue without extra classes, we can use TransactionTemplate.
    private final org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    // Removing @Transactional from the main method to control transaction boundaries manually
    public String placeOrder(String userId, OrderRequest request) {

        // 1. Create and Save Order (Initial State: CREATED, PENDING)
        // Executed in a transaction via TransactionTemplate
        Order order = transactionTemplate.execute(status -> createOrder(userId, request));

        if (order == null) {
            throw new RuntimeException("Failed to create order");
        }

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
            // Use State Service for transition (it has @Transactional)
            orderStateService.transitionOrder(order.getOrderTrackingNumber(), OrderStatus.CANCELLED);
            // We also need to update Payment Status, but StateService only handles OrderStatus.
            // Let's manually update payment status here or enhance StateService.
            // For now, let's allow a direct repository save for payment status in a new transaction block.
            transactionTemplate.execute(status -> {
               Order o = orderRepository.findById(order.getId()).orElse(order);
               o.setPaymentStatus(PaymentStatus.FAILED);
               return orderRepository.save(o);
            });
            return order.getOrderTrackingNumber();
        }

        // 3. Update Order based on Payment Response
        if ("SUCCESS".equals(paymentResponse.getStatus())) {
             // Use State Service for transition (CONFIRMED)
            orderStateService.transitionOrder(order.getOrderTrackingNumber(), OrderStatus.CONFIRMED);

            transactionTemplate.execute(status -> {
               Order o = orderRepository.findById(order.getId()).orElse(order);
               o.setPaymentStatus(PaymentStatus.COMPLETED);
               return orderRepository.save(o);
            });

            // 4. Send Notification via Kafka (Async)
            String message = "Order Placed & Paid! ID: " + order.getOrderTrackingNumber() + " for ₹" + order.getTotalAmount();
            kafkaTemplate.send("order-notifications", message);

            // 5. Clear Cart (Cleanup)
            try {
                cartServiceClient.clearCart(userId);
            } catch (Exception e) {
                log.error("Failed to clear cart for User ID: {}", userId, e);
            }
        } else {
             // Use State Service for transition (CANCELLED)
            orderStateService.transitionOrder(order.getOrderTrackingNumber(), OrderStatus.CANCELLED);
             transactionTemplate.execute(status -> {
               Order o = orderRepository.findById(order.getId()).orElse(order);
               o.setPaymentStatus(PaymentStatus.FAILED);
               return orderRepository.save(o);
            });
        }

        return order.getOrderTrackingNumber();
    }

    // Protected method called inside TransactionTemplate, so @Transactional here is redundant but harmless.
    // The transaction is controlled by the template.
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
}