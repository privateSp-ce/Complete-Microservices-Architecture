package com.foodexpress.restaurant.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI restaurantServiceAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:" + serverPort);
        devServer.setDescription("Restaurant Service - Development");

        Server prodServer = new Server();
        prodServer.setUrl("http://localhost:9000/api/v1");
        prodServer.setDescription("Restaurant Service via API Gateway");

        Contact contact = new Contact();
        contact.setName("FoodExpress Team");
        contact.setEmail("support@foodexpress.com");
        contact.setUrl("https://foodexpress.com");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Restaurant Service API")
                .version("1.0.0")
                .description("FoodExpress Restaurant & Menu Management Service\n\n" +
                        "This service handles:\n" +
                        "- Restaurant registration and management\n" +
                        "- Menu category management\n" +
                        "- Menu item management\n" +
                        "- Restaurant search and discovery\n" +
                        "- Geolocation-based restaurant search")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}
