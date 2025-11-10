package com.spring_cloud.order_service_app.config;

import com.spring_cloud.order_service_app.ErrorDecoder.CustomErrorDecoder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

//    @Bean
//    @LoadBalanced // it "teaches" this RestTemplate how to use Eureka to find other services by their name.
//    public RestTemplate getRestTemplate() {
//        return new RestTemplate();
//    }

    // Manam ippudu 'RestTemplateBuilder' ni aduguthunnam
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Adds this TracingClientHttpRequestInterceptor -> Ee interceptor HTTP request ki trace context (TraceId, SpanId) add chesi next service ki pamputhundi
        return builder.build(); // Spring manaki anni features tho build chesi isthundi
    }

    @Bean
    public CustomErrorDecoder customErrorDecoder() {
        return new CustomErrorDecoder();
    }

}
