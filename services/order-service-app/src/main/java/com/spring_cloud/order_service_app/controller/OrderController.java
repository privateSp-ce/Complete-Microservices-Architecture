package com.spring_cloud.order_service_app.controller;

import com.spring_cloud.order_service_app.client.ProductServiceClient;
import com.spring_cloud.order_service_app.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
//@RequestMapping("/orders") -> no need of this as we are doing this from api gateway itself
//@CrossOrigin(origins = "*") -> no need as we already configured global config cors
@RefreshScope
@Slf4j
public class OrderController {

    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ProductServiceClient productServiceClient;

    @Value("${greeting.message}")
    private String message;

    // 1. Inject the "smart" RestTemplate from AppConfig
    public OrderController(RestTemplate restTemplate, KafkaTemplate<String, String> kafkaTemplate, ProductServiceClient productServiceClient) {
        this.restTemplate = restTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.productServiceClient = productServiceClient;
    }

    @GetMapping("/get-order-using-feignClient")
    public String getOrder() {
        log.info("Came to OrderService. -> Now going to ProductService");
        try {
            return productServiceClient.getProductMessage();
            //return restTemplate.getForObject("http://PRODUCT-SERVICE-APP/products", String.class);
        } catch (Exception e) {
            log.error("Failed to retrieve product message", e);
            throw e;
        }
    }

    @GetMapping("/checking")
    public String getOrderCheck() {
        log.info("Came to OrderService1. -> Now going to ProductService");
//        try {
//            return productServiceClient.getProductMessage();
//            //return restTemplate.getForObject("http://PRODUCT-SERVICE-APP/products", String.class);
//        } catch (Exception e) {
//            log.error("Failed to retrieve product message", e);
//            throw e;
//        }
        return "checking order service";
    }

    // how to handle if product service is down completely
    //    @GetMapping("/get-order-using-feignClient")
    //    @CircuitBreaker(name = "productservicebreaker", fallbackMethod = "productServiceFailed")
    //    public String getOrder() {
    //        // FeignClient uses that ErrorDecoder bean which we created if some error comes here
    //        return productServiceClient.getProductMessage();
    //    }
    //
    //    public String productServiceFailed(Exception e) {
    //        return "Product Service down right now. Please try again later"+e.getMessage();
    //    }
    // ===

    @PostMapping("/post-order-using-feignClient")
    public String postOrder() {

        // 1. Create the data to send
        ProductDTO newProduct = new ProductDTO();
        newProduct.setName("Laptop"); // Example data
        newProduct.setPrice(1200.00);

        return productServiceClient.postProductMessage(newProduct);
    }


    // 2. Create the main endpoint to handle order creation
    @PostMapping("/create-order-using-restTemplate")
    public String createOrder() {
        // FORMAT : URL -> http://SERVICE-ID/endpoint-path
        // 3. Calls Product Service
        String productResponse = restTemplate.getForObject("http://PRODUCT-SERVICE-APP/products", String.class);

        // 4. Call Payment Service
        String paymentResponse = restTemplate.getForObject(
                "http://PAYMENT-SERVICE-APP/payments", // The URL with Service ID
                String.class                        // We expect a String back
        );

        // 5. Send a confirmation message to Kafka
        String notificationMessage = "Order created! Product: " + productResponse + ", Payment: " + paymentResponse;
        kafkaTemplate.send("order-notifications", notificationMessage);

        log.info("Message going to kafka ::: {}", notificationMessage);

        // 6. Combine the responses
        return "Order Created Successfully! \n" +
                " -> Product Status: " + productResponse + "\n" +
                " -> Payment Status: " + paymentResponse + "\n" +
                "-> message from git: " + message;
    }

}
