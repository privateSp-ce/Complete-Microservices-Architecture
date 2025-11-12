package com.spring_cloud.order_service_app.config;

import feign.Capability;
import feign.micrometer.MicrometerCapability;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// => Adds tracing interceptors to Feign clients
// => Propagates trace context (traceId, spanId) in HTTP headers
// => Records metrics about Feign calls (success/failure rates)

@Configuration
public class FeignConfiguration {

    @Bean
    public Capability capability(MeterRegistry registry) {
        return new MicrometerCapability(registry);
    }
}