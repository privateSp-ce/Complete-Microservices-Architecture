package com.foodexpress.order.client;

import com.foodexpress.order.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name = "PRODUCT-SERVICE-APP")
@FeignClient(name = "restaurant-service-app")
public interface ProductServiceClient {

    @GetMapping("/products")
    String getProductMessage();

    // New POST call - Ee method ni complete cheyyi
    @PostMapping("/post-msg-using-feign")
    String postProductMessage(@RequestBody ProductDTO productData);

    // Endpoint kuda marchali, Restaurant Service lo "/api/v1/restaurants/..." undi
    @GetMapping("/api/v1/restaurants/{restaurantId}/menu-items/{itemId}")
    String getMenuItem(@PathVariable("restaurantId") String restaurantId, @PathVariable("itemId") String itemId);

}
