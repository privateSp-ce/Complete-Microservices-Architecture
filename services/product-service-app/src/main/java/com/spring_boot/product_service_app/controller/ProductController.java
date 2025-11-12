package com.spring_boot.product_service_app.controller;

import com.spring_boot.product_service_app.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ProductController {

    @GetMapping("/products")
    public String getMessage() {
        log.info("Reached ProductService");
        return "Hello Dheeraj";
    }

    // === Throwing error manually
    //    @GetMapping("/products")
    //    public String getProductMessage() {
    //        // Instead of returning the message, throw the exception
    //        throw new ResponseStatusException(
    //                HttpStatus.NOT_FOUND, // Tell it to use the 404 status
    //                "Product data not available right now" // Optional message
    //        );
    //        // return "List of all products from Product Service! 📦"; // Comment out the old return
    //    }

    @PostMapping("/post-msg-using-feign")
    public String postMsg(@RequestBody ProductDTO data) {
        return "Product Created Successfully " + data.toString();
    }

}
