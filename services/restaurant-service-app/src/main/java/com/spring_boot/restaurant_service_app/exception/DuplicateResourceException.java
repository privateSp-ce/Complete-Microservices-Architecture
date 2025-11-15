package com.spring_boot.restaurant_service_app.exception;

/**
 * Thrown when attempting to create a resource that already exists
 * Example: Owner already has a restaurant, Duplicate menu item
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}