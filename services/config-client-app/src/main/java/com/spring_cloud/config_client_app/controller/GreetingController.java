package com.spring_cloud.config_client_app.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RefreshScope
public class GreetingController {

    private final RestTemplate restTemplate;
    // The value is injected here from the properties file
    @Value("${greeting.message}")
    private String message;

    public GreetingController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // This endpoint will display the message
    @GetMapping("/greeting")
    public String getGreeting() {
        return message;
    }

    @GetMapping("/fetchFromProduct")
    @CircuitBreaker(name = "productServiceBreaker", fallbackMethod = "getProductFallback") // Adding Switch to turn on / off
    public String callToProductService() {
        // Call product-service using its name, not its address
        String productMessage = restTemplate.getForObject("http://PRODUCT-SERVICE-APP/products", String.class);
        return "Message from config: (property configured in git) '" + message + "' ---- Response from Product Service: '" + productMessage + "'";
    }

    public String getProductFallback(Exception ex) {
        return "Product Service is currently down. Please try again later! 😥"+ ex.getMessage();
    }

}
