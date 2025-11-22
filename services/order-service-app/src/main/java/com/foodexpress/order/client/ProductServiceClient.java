package com.foodexpress.order.client;

import com.foodexpress.order.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PRODUCT-SERVICE-APP")
public interface ProductServiceClient {

    @GetMapping("/products")
    String getProductMessage();

    // New POST call - Ee method ni complete cheyyi
    @PostMapping("/post-msg-using-feign")
    String postProductMessage(@RequestBody ProductDTO productData);

}
