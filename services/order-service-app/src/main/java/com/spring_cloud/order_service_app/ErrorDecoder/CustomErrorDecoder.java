package com.spring_cloud.order_service_app.ErrorDecoder;

import com.spring_cloud.order_service_app.exception.ProductNotFoundException;
import com.spring_cloud.order_service_app.exception.ServiceDownException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

public class CustomErrorDecoder implements ErrorDecoder {

    private static final Logger log = LoggerFactory.getLogger(CustomErrorDecoder.class);
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Error calling Feign client. Status: {}, Method: {}", response.status(), methodKey);

        // Check the HTTP status code from the response
        switch (response.status()) {
            case 404:
                log.warn("Resource not found (404) for method: {}", methodKey);
                // Return our specific "Not Found" exception
                return new ProductNotFoundException("Resource not found via Feign: " + methodKey);

            case 500:
            case 503: // Service Unavailable
                log.error("Downstream service error ({}) for method: {}", response.status(), methodKey);
                // Return our specific "Service Down" exception
                return new ServiceDownException("Downstream service is unavailable: " + methodKey);

            default:
                // For any other error, let the default Feign decoder handle it
                log.error("Unhandled Feign error status: {} for method: {}", response.status(), methodKey);
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}