package com.foodexpress.order.service;

import com.foodexpress.order.entity.Order;
import com.foodexpress.order.enums.OrderStatus;
import com.foodexpress.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderStateMachineService {

    private final OrderRepository orderRepository;

    @Transactional
    public void updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        validateTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    private void validateTransition(OrderStatus current, OrderStatus next) {
        // Simple state machine logic
        // Only allow forward progression or cancellation from non-terminal states
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED || current == OrderStatus.FAILED) {
            throw new IllegalStateException("Cannot change status from terminal state: " + current);
        }

        if (next == OrderStatus.CANCELLED) {
             return; // Allow cancellation from any non-terminal state
        }

        // Define allowed forward transitions
        // CREATED -> CONFIRMED -> PREPARING -> READY -> OUT -> DELIVERED
        boolean isValid = false;
        switch (current) {
            case CREATED:
                isValid = (next == OrderStatus.CONFIRMED);
                break;
            case CONFIRMED:
                isValid = (next == OrderStatus.PREPARING);
                break;
            case PREPARING:
                isValid = (next == OrderStatus.READY_FOR_PICKUP);
                break;
            case READY_FOR_PICKUP:
                isValid = (next == OrderStatus.OUT_FOR_DELIVERY);
                break;
            case OUT_FOR_DELIVERY:
                isValid = (next == OrderStatus.DELIVERED);
                break;
            default:
                break;
        }

        if (!isValid) {
            throw new IllegalStateException("Invalid state transition from " + current + " to " + next);
        }
    }
}
