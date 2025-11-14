package com.spring_cloud.config_client_app;

import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConfigClientAppApplication {

	public static void main(final String[] args) {
        System.out.println("Hello");
        MDC.put("hello", "123");
        SpringApplication.run(ConfigClientAppApplication.class, args);
	}

}
