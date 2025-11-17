package com.spring_boot.cart_service_app.exception;


public class InvalidCartOperationException extends RuntimeException {

    public InvalidCartOperationException(String message) {
        super(message);
    }

    public InvalidCartOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}