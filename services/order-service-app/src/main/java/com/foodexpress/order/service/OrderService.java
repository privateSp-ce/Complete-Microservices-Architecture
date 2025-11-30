package com.foodexpress.order.service;

import com.foodexpress.order.client.CartServiceClient;
import com.foodexpress.order.client.PaymentServiceClient;
import com.foodexpress.order.dto.CartResponse;
import com.foodexpress.order.dto.OrderRequest;
import com.foodexpress.order.dto.PaymentRequest;
import com.foodexpress.order.dto.PaymentResponse;
import com.foodexpress.order.entity.Order;
import com.foodexpress.order.entity.OrderItem;
import com.foodexpress.order.enums.OrderEvent;
import com.foodexpress.order.enums.OrderStatus;
import com.foodexpress.order.enums.PaymentStatus;
import com.foodexpress.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
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
    private final StateMachineFactory<OrderStatus, OrderEvent> stateMachineFactory;

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
            sendEvent(order, OrderEvent.FAIL_PAYMENT); // Transition using SM
            order.setPaymentStatus(PaymentStatus.FAILED);
            orderRepository.save(order);
            return order.getOrderTrackingNumber();
        }

        // 3. Update Order based on Payment Response
        if ("SUCCESS".equals(paymentResponse.getStatus())) {
            sendEvent(order, OrderEvent.CONFIRM_ORDER); // Transition using SM
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            orderRepository.save(order); // Explicit save as SM interceptor is not set up yet

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
            sendEvent(order, OrderEvent.FAIL_PAYMENT); // Transition using SM
            order.setPaymentStatus(PaymentStatus.FAILED);
            orderRepository.save(order);
        }

        return order.getOrderTrackingNumber();
    }

    @Transactional
    public void cancelOrder(String orderTrackingNumber) {
        Order order = getOrderByTrackingNumber(orderTrackingNumber);
        sendEvent(order, OrderEvent.CANCEL_ORDER);
        orderRepository.save(order);
    }

    @Transactional
    public void startPreparation(String orderTrackingNumber) {
        Order order = getOrderByTrackingNumber(orderTrackingNumber);
        sendEvent(order, OrderEvent.START_PREPARATION);
        orderRepository.save(order);
    }

    @Transactional
    public void readyForPickup(String orderTrackingNumber) {
        Order order = getOrderByTrackingNumber(orderTrackingNumber);
        sendEvent(order, OrderEvent.READY_FOR_PICKUP);
        orderRepository.save(order);
    }

    @Transactional
    public void startDelivery(String orderTrackingNumber) {
        Order order = getOrderByTrackingNumber(orderTrackingNumber);
        sendEvent(order, OrderEvent.START_DELIVERY);
        orderRepository.save(order);
    }

    @Transactional
    public void completeDelivery(String orderTrackingNumber) {
        Order order = getOrderByTrackingNumber(orderTrackingNumber);
        sendEvent(order, OrderEvent.COMPLETE_DELIVERY);
        orderRepository.save(order);
    }

    private Order getOrderByTrackingNumber(String orderTrackingNumber) {
        return orderRepository.findByOrderTrackingNumber(orderTrackingNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));
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

    private void sendEvent(Order order, OrderEvent event) {
        StateMachine<OrderStatus, OrderEvent> stateMachine = build(order);
        if (!stateMachine.sendEvent(event)) {
            log.error("Transition failed: {} -> {} using event {}", order.getStatus(), event, event);
            throw new RuntimeException("Invalid state transition");
        }
        order.setStatus(stateMachine.getState().getId());
    }

    private StateMachine<OrderStatus, OrderEvent> build(Order order) {
        StateMachine<OrderStatus, OrderEvent> stateMachine = stateMachineFactory.getStateMachine(order.getOrderTrackingNumber());
        stateMachine.stop();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.resetStateMachine(new DefaultStateMachineContext<>(order.getStatus(), null, null, null));
                });
        stateMachine.start();
        return stateMachine;
    }
}
