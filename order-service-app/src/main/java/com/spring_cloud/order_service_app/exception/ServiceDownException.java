package com.spring_cloud.order_service_app.exception;

public class ServiceDownException extends RuntimeException {
    public ServiceDownException(String message) {
        super(message);
    }
}