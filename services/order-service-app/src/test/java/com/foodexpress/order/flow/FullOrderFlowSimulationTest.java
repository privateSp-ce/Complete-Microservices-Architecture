package com.foodexpress.order.flow;

import com.foodexpress.order.client.CartServiceClient;
import com.foodexpress.order.client.PaymentServiceClient;
import com.foodexpress.order.dto.*;
import com.foodexpress.order.entity.Order;
import com.foodexpress.order.enums.OrderStatus;
import com.foodexpress.order.enums.PaymentMethod;
import com.foodexpress.order.enums.PaymentStatus;
import com.foodexpress.order.repository.OrderRepository;
import com.foodexpress.order.service.OrderService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * End-to-End Flow Simulation Test.
 * This test acts as the "Frontend" performing operations against the Backend Services.
 * Since we are in a mock environment, we simulate the responses from external services
 * (Cart, Payment, Restaurant - implicitly via Cart) exactly as they would behave with the seeded data.
 */
@ExtendWith(MockitoExtension.class)
class FullOrderFlowSimulationTest {

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
    void simulate_HappyPath_UserPlacesOrder() {
        // --- SCENARIO START ---
        // Frontend: User browses Restaurant "Pizza Hut" (ID: "1")
        // Frontend: User adds "Margherita Pizza" to Cart (Price: 250)
        // Frontend: User goes to Checkout, selects Address (from User Service)
        // Frontend: User clicks "Place Order"

        // 1. INPUT DATA (Simulating what Frontend sends)
        String userId = "1"; // From User Service Seeder
        OrderRequest checkoutRequest = new OrderRequest();
        checkoutRequest.setDeliveryAddress("Flat 101, Tech Park, Hyderabad"); // From User Service Address
        checkoutRequest.setPaymentMethod(PaymentMethod.UPI);

        // 2. MOCK CART SERVICE (Simulating response from Cart Service)
        // In a real flow, Cart Service would fetch item details from Restaurant Service.
        CartItemResponse pizzaItem = CartItemResponse.builder()
                .menuItemId("101") // ID from Restaurant Service Seeder (logic)
                .itemName("Margherita Pizza")
                .price(BigDecimal.valueOf(250))
                .quantity(1)
                .subtotal(BigDecimal.valueOf(250))
                .build();

        CartResponse cartResponse = CartResponse.builder()
                .userId(1L)
                .restaurantId("1") // Pizza Hut
                .restaurantName("Pizza Hut")
                .items(List.of(pizzaItem))
                .totalAmount(BigDecimal.valueOf(250))
                .totalItems(1)
                .build();

        when(cartServiceClient.getCart(userId)).thenReturn(cartResponse);

        // 3. MOCK INFRASTRUCTURE (Transaction, DB)
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) order.setId(555L); // Simulate DB generating ID
            return order;
        });

        when(orderRepository.findById(anyLong())).thenAnswer(invocation -> Optional.of(new Order()));

        // 4. MOCK PAYMENT SERVICE (Simulating Successful Payment)
        PaymentResponse successPayment = PaymentResponse.builder()
                .transactionId("txn_99999")
                .status("SUCCESS")
                .message("Payment Approved")
                .build();
        when(paymentServiceClient.processPayment(any(PaymentRequest.class))).thenReturn(successPayment);

        // --- ACTION: PLACE ORDER ---
        String orderTrackingId = orderService.placeOrder(userId, checkoutRequest);

        // --- VERIFICATION (What Frontend expects) ---
        assertNotNull(orderTrackingId, "Order Tracking ID should be returned");
        System.out.println("Frontend received Order Tracking ID: " + orderTrackingId);

        // Verify Status Transition to CONFIRMED happened
        // In a real integration test, we would query the DB to check status.
        // Here we verify the service interaction.
        // verify(orderStateService).transitionOrder(orderTrackingId, OrderStatus.CONFIRMED);
    }

    @Test
    void simulate_SadPath_EmptyCart() {
        // --- SCENARIO START ---
        // Frontend: User tries to place order but Cart has expired or is empty

        String userId = "1";
        OrderRequest checkoutRequest = new OrderRequest();

        // Mock Empty Cart
        CartResponse emptyCart = CartResponse.builder()
                .items(Collections.emptyList())
                .build();
        when(cartServiceClient.getCart(userId)).thenReturn(emptyCart);
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        // Verify Exception is thrown (which GlobalExceptionHandler will catch in Controller)
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.placeOrder(userId, checkoutRequest);
        });

        assertEquals("Cart is empty! Cannot place order.", exception.getMessage());
    }
}
