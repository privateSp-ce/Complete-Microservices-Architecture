package com.foodexpress.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
// === Hey, ee project lo @FeignClient ane annotation tho mark chesina interfaces ni vethiki, vaatiki actual implementation
// (network call chese code) ni automatic ga create cheyyi ===
@EnableFeignClients // <-- To enable feign clients in our application
public class OrderServiceAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceAppApplication.class, args);
	}

}
