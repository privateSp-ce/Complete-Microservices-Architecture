package com.spring_cloud.user_service_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single error detail (used for validation errors)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorDetail {

    /**
     * Field name where error occurred (e.g., "email", "password")
     */
    private String field;

    /**
     * Error message for this field
     */
    private String message;

    /**
     * Rejected value (optional, for debugging)
     */
    private Object rejectedValue;

    /**
     * Error code (optional, for internationalization)
     */
    private String code;

    // ============================================
    // Convenience Constructors
    // ============================================

    public ErrorDetail(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public ErrorDetail(String field, String message, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.rejectedValue = rejectedValue;
    }
}