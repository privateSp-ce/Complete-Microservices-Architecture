package com.foodexpress.cart.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8086}")
    private String serverPort;

    @Bean
    public OpenAPI cartServiceAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:" + serverPort);
        devServer.setDescription("Cart Service - Development");

        Server prodServer = new Server();
        prodServer.setUrl("http://localhost:9000");
        prodServer.setDescription("Cart Service via API Gateway");

        Contact contact = new Contact();
        contact.setName("FoodExpress Team");
        contact.setEmail("support@foodexpress.com");
        contact.setUrl("https://foodexpress.com");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Cart Service API")
                .version("1.0.0")
                .description("""
                        # Cart Service API Documentation
                        
                        This service handles shopping cart operations for the FoodExpress platform.
                        
                        ## Features
                        - Add items to cart
                        - Update cart item quantities
                        - Remove items from cart
                        - Clear entire cart
                        - Cart validation before checkout
                        - Redis caching for performance
                        
                        ## Notes
                        - Carts expire after 30 minutes of inactivity
                        - Maximum 50 items per cart
                        - Items from only one restaurant allowed per cart
                        - All endpoints require X-User-Id header
                        
                        ## Tech Stack
                        - Spring Boot 3.5.6
                        - MySQL 8.0
                        - Redis for caching
                        - Java 21
                        """)
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}