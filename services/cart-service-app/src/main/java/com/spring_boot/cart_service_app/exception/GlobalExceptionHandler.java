package com.spring_boot.cart_service_app.exception;

import com.spring_boot.cart_service_app.dto.common.ApiResponse;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final Tracer tracer;

    public GlobalExceptionHandler(@Autowired(required = false) Tracer tracer) {
        this.tracer = tracer;
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "no-trace-id";
    }

    /**
     * Handle CartNotFoundException (404)
     */
    @ExceptionHandler(CartNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse<Void>> handleCartNotFoundException(CartNotFoundException ex) {
        String traceId = getTraceId();
        log.warn("Cart not found. TraceId: {}, Message: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle InvalidCartOperationException (400)
     */
    @ExceptionHandler(InvalidCartOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCartOperationException(InvalidCartOperationException ex) {
        String traceId = getTraceId();
        log.warn("Invalid cart operation. TraceId: {}, Message: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle Validation Errors (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        String traceId = getTraceId();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed. TraceId: {}, Errors: {}", traceId, errors);

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle Generic Exceptions (500)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        String traceId = getTraceId();
        log.error("Unexpected error occurred. TraceId: {}", traceId, ex);

        ApiResponse<Void> response = ApiResponse.error(
                "An unexpected error occurred. Please try again later.",
                traceId
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}