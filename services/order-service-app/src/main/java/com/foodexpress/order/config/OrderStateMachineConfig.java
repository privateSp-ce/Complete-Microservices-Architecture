package com.foodexpress.order.config;

import com.foodexpress.order.enums.OrderEvent;
import com.foodexpress.order.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
@Slf4j
public class OrderStateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderStatus, OrderEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderEvent> states) throws Exception {
        states
            .withStates()
            .initial(OrderStatus.CREATED)
            .states(EnumSet.allOf(OrderStatus.class))
            .end(OrderStatus.DELIVERED)
            .end(OrderStatus.CANCELLED)
            .end(OrderStatus.FAILED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderEvent> transitions) throws Exception {
        transitions
            .withExternal().source(OrderStatus.CREATED).target(OrderStatus.CONFIRMED).event(OrderEvent.CONFIRM_ORDER)
            .and()
            .withExternal().source(OrderStatus.CREATED).target(OrderStatus.CANCELLED).event(OrderEvent.CANCEL_ORDER)
            .and()
            .withExternal().source(OrderStatus.CREATED).target(OrderStatus.FAILED).event(OrderEvent.FAIL_PAYMENT)
            .and()
            .withExternal().source(OrderStatus.CONFIRMED).target(OrderStatus.PREPARING).event(OrderEvent.START_PREPARATION)
            .and()
            .withExternal().source(OrderStatus.CONFIRMED).target(OrderStatus.CANCELLED).event(OrderEvent.CANCEL_ORDER)
            .and()
            .withExternal().source(OrderStatus.PREPARING).target(OrderStatus.READY_FOR_PICKUP).event(OrderEvent.READY_FOR_PICKUP)
            .and()
            .withExternal().source(OrderStatus.PREPARING).target(OrderStatus.CANCELLED).event(OrderEvent.CANCEL_ORDER)
            .and()
            .withExternal().source(OrderStatus.READY_FOR_PICKUP).target(OrderStatus.OUT_FOR_DELIVERY).event(OrderEvent.START_DELIVERY)
            .and()
            .withExternal().source(OrderStatus.OUT_FOR_DELIVERY).target(OrderStatus.DELIVERED).event(OrderEvent.COMPLETE_DELIVERY);
    }
}
