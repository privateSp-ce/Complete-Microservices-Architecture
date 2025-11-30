package com.foodexpress.order.service;

import com.foodexpress.order.client.CartServiceClient;
import com.foodexpress.order.client.PaymentServiceClient;
import com.foodexpress.order.dto.CartItemResponse;
import com.foodexpress.order.dto.CartResponse;
import com.foodexpress.order.dto.OrderRequest;
import com.foodexpress.order.dto.PaymentRequest;
import com.foodexpress.order.dto.PaymentResponse;
import com.foodexpress.order.entity.Order;
import com.foodexpress.order.enums.OrderEvent;
import com.foodexpress.order.enums.OrderStatus;
import com.foodexpress.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.state.State;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceStateMachineTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartServiceClient cartServiceClient;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private StateMachineFactory<OrderStatus, OrderEvent> stateMachineFactory;

    @Mock
    private StateMachine<OrderStatus, OrderEvent> stateMachine;

    @Mock
    private StateMachineAccessor<OrderStatus, OrderEvent> stateMachineAccessor;

    @Mock
    private State<OrderStatus, OrderEvent> state;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        // Common setup for state machine mocking
    }

    private void mockStateMachine(OrderStatus currentStatus, boolean transitionResult, OrderStatus nextStatus) {
        when(stateMachineFactory.getStateMachine(anyString())).thenReturn(stateMachine);
        when(stateMachine.getStateMachineAccessor()).thenReturn(stateMachineAccessor);
        doAnswer(invocation -> null).when(stateMachineAccessor).doWithAllRegions(any());
        when(stateMachine.sendEvent(any(OrderEvent.class))).thenReturn(transitionResult);
        if (transitionResult) {
            when(stateMachine.getState()).thenReturn(state);
            when(state.getId()).thenReturn(nextStatus);
        }
    }

    @Test
    void testPlaceOrderSuccess() {
        String userId = "123";
        OrderRequest request = new OrderRequest();
        request.setDeliveryAddress("Test Address");

        CartResponse cart = new CartResponse();
        cart.setRestaurantId("rest1");
        cart.setRestaurantName("Test Rest");
        cart.setTotalAmount(new BigDecimal("100.00"));
        cart.setTotalItems(1);
        CartItemResponse item = new CartItemResponse();
        item.setMenuItemId("menu1");
        item.setItemName("Food");
        item.setPrice(new BigDecimal("100.00"));
        item.setQuantity(1);
        item.setSubtotal(new BigDecimal("100.00"));
        cart.setItems(Collections.singletonList(item));

        when(cartServiceClient.getCart(userId)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setStatus("SUCCESS");
        when(paymentServiceClient.processPayment(any(PaymentRequest.class))).thenReturn(paymentResponse);

        // Mock State Machine for CONFIRM_ORDER
        mockStateMachine(OrderStatus.CREATED, true, OrderStatus.CONFIRMED);

        String orderId = orderService.placeOrder(userId, request);

        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(kafkaTemplate).send(eq("order-notifications"), anyString());
        verify(cartServiceClient).clearCart(userId);
    }

    @Test
    void testStartPreparation() {
        String trackingId = "order-123";
        Order order = new Order();
        order.setOrderTrackingNumber(trackingId);
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findByOrderTrackingNumber(trackingId)).thenReturn(Optional.of(order));

        mockStateMachine(OrderStatus.CONFIRMED, true, OrderStatus.PREPARING);

        orderService.startPreparation(trackingId);

        assertEquals(OrderStatus.PREPARING, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void testInvalidTransition() {
        String trackingId = "order-123";
        Order order = new Order();
        order.setOrderTrackingNumber(trackingId);
        order.setStatus(OrderStatus.CREATED); // Can't go to PREPARING directly

        when(orderRepository.findByOrderTrackingNumber(trackingId)).thenReturn(Optional.of(order));

        // Mock fail
        when(stateMachineFactory.getStateMachine(anyString())).thenReturn(stateMachine);
        when(stateMachine.getStateMachineAccessor()).thenReturn(stateMachineAccessor);
        doAnswer(invocation -> null).when(stateMachineAccessor).doWithAllRegions(any());
        when(stateMachine.sendEvent(any(OrderEvent.class))).thenReturn(false);

        assertThrows(RuntimeException.class, () -> orderService.startPreparation(trackingId));
    }
}
