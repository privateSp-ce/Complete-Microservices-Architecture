package com.foodexpress.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 * Provides interactive API documentation at /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8085}")
    private String serverPort;

    @Bean
    public OpenAPI userServiceOpenAPI() {
        // Server configuration
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Local Development Server");

        Server productionServer = new Server()
                .url("https://api.foodexpress.com")
                .description("Production Server");

        // Contact information
        Contact contact = new Contact()
                .name("FoodExpress Support")
                .email("support@foodexpress.com")
                .url("https://foodexpress.com/support");

        // License information
        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html");

        // API information
        Info info = new Info()
                .title("User Service API")
                .version("1.0.0")
                .description("""
                        # User Service API Documentation
                        
                        This service handles user authentication and profile management for the FoodExpress platform.
                        
                        ## Features
                        - User Registration & Login with JWT authentication
                        - User Profile Management
                        - Address Management (CRUD operations)
                        - Password Management
                        - Email & Phone availability checks
                        
                        ## Authentication
                        Most endpoints require JWT authentication. After login, include the JWT token in the Authorization header:
                       
                        ## Getting Started
                        1. Register a new user: `POST /api/v1/auth/register`
                        2. Login to get JWT token: `POST /api/v1/auth/login`
                        3. Use the token to access protected endpoints
                        
                        ## Tech Stack
                        - Spring Boot 3.5.7
                        - Spring Security with JWT
                        - MySQL 8.0
                        - Java 21
                        """)
                .contact(contact)
                .license(license);

        // Security scheme (JWT)
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("JWT Authentication")
                .description("Enter JWT token obtained from login endpoint");

        // Security requirement
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, productionServer))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}