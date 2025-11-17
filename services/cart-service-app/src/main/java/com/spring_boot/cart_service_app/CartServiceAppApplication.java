package com.spring_boot.cart_service_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
public class CartServiceAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(CartServiceAppApplication.class, args);
	}

}
