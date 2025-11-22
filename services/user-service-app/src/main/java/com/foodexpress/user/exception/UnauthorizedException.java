package com.foodexpress.user.exception;

/**
 * Thrown when authentication fails or token is invalid
 * Example: Invalid credentials, Expired token, Invalid JWT
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}