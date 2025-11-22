package com.foodexpress.restaurant.exception;

/**
 * Thrown when request data is invalid or business logic validation fails
 * Example: Invalid opening hours, Invalid price, Business rule violations
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}