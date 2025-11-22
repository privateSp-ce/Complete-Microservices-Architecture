package com.foodexpress.cart.client;

import com.foodexpress.cart.dto.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/internal/{userId}/exists")
    ApiResponse<Boolean> checkUserExists(@PathVariable("userId") Long userId);
}