package com.foodexpress.order.service;

import com.foodexpress.order.client.CartServiceClient;
import com.foodexpress.order.client.PaymentServiceClient;
import com.foodexpress.order.dto.*;
import com.foodexpress.order.entity.Order;
import com.foodexpress.order.enums.OrderStatus;
import com.foodexpress.order.enums.PaymentStatus;
import com.foodexpress.order.repository.OrderRepository;
import com.foodexpress.order.service.statemachine.OrderStateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartServiceClient cartServiceClient;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private OrderStateService orderStateService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Spy
    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_HappyPath_PaymentSuccess() {
        // Arrange
        String userId = "123";
        OrderRequest request = new OrderRequest();
        request.setDeliveryAddress("123 Street");

        CartItemResponse cartItem = new CartItemResponse();
        cartItem.setMenuItemId("1");
        cartItem.setItemName("Pizza");
        cartItem.setPrice(BigDecimal.valueOf(10.0));
        cartItem.setQuantity(2);
        cartItem.setSubtotal(BigDecimal.valueOf(20.0));

        CartResponse cartResponse = new CartResponse();
        cartResponse.setRestaurantId("1");
        cartResponse.setRestaurantName("Pizza Hut");
        cartResponse.setTotalAmount(BigDecimal.valueOf(20.0));
        cartResponse.setTotalItems(2);
        cartResponse.setItems(List.of(cartItem));

        when(cartServiceClient.getCart(userId)).thenReturn(cartResponse);

        // Mock TransactionTemplate
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        // Mock Order Save
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        // Mock findById for status updates
        when(orderRepository.findById(anyLong())).thenAnswer(invocation -> Optional.of(new Order()));

        PaymentResponse paymentResponse = new PaymentResponse("txn_123", "SUCCESS", "Paid");
        when(paymentServiceClient.processPayment(any(PaymentRequest.class))).thenReturn(paymentResponse);

        // Act
        String trackingId = orderService.placeOrder(userId, request);

        // Assert
        assertNotNull(trackingId);
        verify(cartServiceClient).getCart(userId);
        verify(orderRepository, atLeast(2)).save(any(Order.class));
        verify(paymentServiceClient).processPayment(any(PaymentRequest.class));
        verify(kafkaTemplate).send(eq("order-notifications"), anyString());
        verify(cartServiceClient).clearCart(userId);
        verify(orderStateService).transitionOrder(anyString(), eq(OrderStatus.CONFIRMED));
    }

    @Test
    void placeOrder_SadPath_PaymentFailed() {
        // Arrange
        String userId = "123";
        OrderRequest request = new OrderRequest();
        request.setDeliveryAddress("123 Street");

        CartItemResponse cartItem = new CartItemResponse();
        cartItem.setMenuItemId("1");
        cartItem.setItemName("Pizza");
        cartItem.setPrice(BigDecimal.valueOf(10.0));
        cartItem.setQuantity(2);
        cartItem.setSubtotal(BigDecimal.valueOf(20.0));

        CartResponse cartResponse = new CartResponse();
        cartResponse.setRestaurantId("1");
        cartResponse.setRestaurantName("Pizza Hut");
        cartResponse.setTotalAmount(BigDecimal.valueOf(20.0));
        cartResponse.setTotalItems(2);
        cartResponse.setItems(List.of(cartItem));

        when(cartServiceClient.getCart(userId)).thenReturn(cartResponse);

        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        when(orderRepository.findById(anyLong())).thenAnswer(invocation -> Optional.of(new Order()));

        PaymentResponse paymentResponse = new PaymentResponse("txn_123", "FAILED", "Insufficient Funds");
        when(paymentServiceClient.processPayment(any(PaymentRequest.class))).thenReturn(paymentResponse);

        // Act
        String trackingId = orderService.placeOrder(userId, request);

        // Assert
        assertNotNull(trackingId);
        verify(cartServiceClient).getCart(userId);
        verify(orderRepository, atLeast(2)).save(any(Order.class));
        verify(paymentServiceClient).processPayment(any(PaymentRequest.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString());
        verify(cartServiceClient, never()).clearCart(userId);
        verify(orderStateService).transitionOrder(anyString(), eq(OrderStatus.CANCELLED));
    }
}
