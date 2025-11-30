package com.foodexpress.order.service;

import com.foodexpress.order.client.CartServiceClient;
import com.foodexpress.order.client.PaymentServiceClient;
import com.foodexpress.order.dto.*;
import com.foodexpress.order.entity.Order;
import com.foodexpress.order.enums.OrderStatus;
import com.foodexpress.order.enums.PaymentStatus;
import com.foodexpress.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

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
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        PaymentResponse paymentResponse = new PaymentResponse("txn_123", "SUCCESS", "Paid");
        when(paymentServiceClient.processPayment(any(PaymentRequest.class))).thenReturn(paymentResponse);

        // Act
        String trackingId = orderService.placeOrder(userId, request);

        // Assert
        assertNotNull(trackingId);
        verify(cartServiceClient).getCart(userId);
        verify(orderRepository, atLeast(2)).save(any(Order.class)); // Saved initially, then updated
        verify(paymentServiceClient).processPayment(any(PaymentRequest.class));
        verify(kafkaTemplate).send(eq("order-notifications"), anyString());
        verify(cartServiceClient).clearCart(userId);
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
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        PaymentResponse paymentResponse = new PaymentResponse("txn_123", "FAILED", "Insufficient Funds");
        when(paymentServiceClient.processPayment(any(PaymentRequest.class))).thenReturn(paymentResponse);

        // Act
        String trackingId = orderService.placeOrder(userId, request);

        // Assert
        assertNotNull(trackingId);
        verify(cartServiceClient).getCart(userId);
        verify(orderRepository, atLeast(2)).save(any(Order.class));
        verify(paymentServiceClient).processPayment(any(PaymentRequest.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString()); // Notification should NOT be sent
        // verify(cartServiceClient, never()).clearCart(userId); // Cart should NOT be cleared (actually it's inside try catch but logic says it's called only if success)
        verify(cartServiceClient, never()).clearCart(userId);
    }
}
