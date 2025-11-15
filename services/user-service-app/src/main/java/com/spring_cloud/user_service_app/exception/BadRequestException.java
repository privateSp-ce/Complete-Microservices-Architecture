package com.spring_cloud.user_service_app.exception;

/**
 * Thrown when request data is invalid or business logic validation fails
 * Example: Passwords don't match, Invalid operation
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}