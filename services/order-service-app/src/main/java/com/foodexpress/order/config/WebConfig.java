package com.foodexpress.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//For cross origin policy (to remove CORS issues we are allowing from all origins)
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Anni paths ki ("/**") apply cheyyi
                        .allowedOrigins("*")   // Ye origin nunchi ayina allow cheyyi
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Ee methods allow cheyyi
                        .allowedHeaders("*")   // Anni headers allow cheyyi
                        .allowCredentials(false); // Credentials (like cookies) allow cheyyaku (security best practice with *)
            }
        };
    }
}