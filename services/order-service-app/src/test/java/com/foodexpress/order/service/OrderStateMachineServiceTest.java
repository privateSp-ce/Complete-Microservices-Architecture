package com.foodexpress.order.service;

import com.foodexpress.order.entity.Order;
import com.foodexpress.order.enums.OrderStatus;
import com.foodexpress.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderStateMachineServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderStateMachineService stateMachineService;

    @Test
    void updateStatus_ValidTransition_ShouldUpdate() {
        // Arrange
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        stateMachineService.updateStatus(orderId, OrderStatus.CONFIRMED);

        // Assert
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateStatus_InvalidTransition_ShouldThrowException() {
        // Arrange
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert (CREATED -> DELIVERED is invalid)
        assertThrows(IllegalStateException.class, () ->
            stateMachineService.updateStatus(orderId, OrderStatus.DELIVERED)
        );

        // Assert status didn't change
        assertEquals(OrderStatus.CREATED, order.getStatus());
        verify(orderRepository, never()).save(order);
    }

    @Test
    void updateStatus_CancelFromAnyState_ShouldSucceed() {
        // Arrange
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PREPARING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        stateMachineService.updateStatus(orderId, OrderStatus.CANCELLED);

        // Assert
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }
}
