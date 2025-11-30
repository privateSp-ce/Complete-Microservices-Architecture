package com.foodexpress.order.service.statemachine;

import com.foodexpress.order.entity.Order;
import com.foodexpress.order.enums.OrderStatus;
import com.foodexpress.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStateService {

    private final OrderRepository orderRepository;

    // Define valid transitions
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.CREATED, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED, OrderStatus.FAILED),
            OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING, EnumSet.of(OrderStatus.READY_FOR_PICKUP),
            OrderStatus.READY_FOR_PICKUP, EnumSet.of(OrderStatus.OUT_FOR_DELIVERY),
            OrderStatus.OUT_FOR_DELIVERY, EnumSet.of(OrderStatus.DELIVERED, OrderStatus.FAILED),
            OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.FAILED, EnumSet.noneOf(OrderStatus.class)
    );

    @Transactional
    public void transitionOrder(String orderTrackingNumber, OrderStatus newStatus) {
        Order order = orderRepository.findByOrderTrackingNumber(orderTrackingNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderTrackingNumber));

        validateTransition(order.getStatus(), newStatus);

        log.info("Transitioning Order {} from {} to {}", orderTrackingNumber, order.getStatus(), newStatus);
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    private void validateTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        Set<OrderStatus> allowedNextStates = VALID_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(OrderStatus.class));
        if (!allowedNextStates.contains(newStatus)) {
            throw new IllegalStateException(
                    String.format("Invalid state transition from %s to %s", currentStatus, newStatus));
        }
    }
}
