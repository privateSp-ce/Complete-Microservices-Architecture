package com.foodexpress.cart.client;

import com.foodexpress.cart.dto.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurant-service")
public interface RestaurantServiceClient {

    @GetMapping("/api/v1/restaurants/{id}")
    ApiResponse<Object> getRestaurantById(@PathVariable("id") Long id);

    @GetMapping("/api/v1/restaurants/{restaurantId}/menu/items/{itemId}")
    ApiResponse<Object> getMenuItem(
            @PathVariable("restaurantId") Long restaurantId,
            @PathVariable("itemId") Long itemId
    );
}