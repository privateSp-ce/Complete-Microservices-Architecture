package com.spring_cloud.user_service_app.exception;

import com.spring_cloud.user_service_app.dto.ApiResponse;
import com.spring_cloud.user_service_app.dto.ErrorDetail;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the entire application
 * Catches all exceptions and returns consistent error responses
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Tracer tracer;

    /**
     * Get current trace ID for distributed tracing
     */
    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "N/A";
    }

    /**
     * Handle validation errors from @Valid annotation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String traceId = getTraceId();

        List<ErrorDetail> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorDetail(
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue()
                ))
                .collect(Collectors.toList());

        log.warn("Validation failed. TraceId: {}, Errors: {}", traceId, errors.size());

        ApiResponse<Void> response = ApiResponse.error("Validation failed", errors, traceId);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle ResourceNotFoundException (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        String traceId = getTraceId();
        log.warn("Resource not found. TraceId: {}, Message: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle DuplicateResourceException (409 Conflict)
     */
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(DuplicateResourceException ex) {
        String traceId = getTraceId();
        log.warn("Duplicate resource. TraceId: {}, Message: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handle UnauthorizedException (401)
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(UnauthorizedException ex) {
        String traceId = getTraceId();
        log.warn("Unauthorized access. TraceId: {}, Message: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle BadRequestException (400)
     */
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(BadRequestException ex) {
        String traceId = getTraceId();
        log.warn("Bad request. TraceId: {}, Message: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle Spring Security authentication failures
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(Exception ex) {
        String traceId = getTraceId();
        log.warn("Authentication failed. TraceId: {}, Message: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error("Invalid credentials", traceId);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle IllegalArgumentException (400)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        String traceId = getTraceId();
        log.warn("Illegal argument. TraceId: {}, Message: {}", traceId, ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle all other exceptions (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        String traceId = getTraceId();
        log.error("Unexpected error occurred. TraceId: {}, Error: {}", traceId, ex.getMessage(), ex);

        // Don't expose internal error details to client
        ApiResponse<Void> response = ApiResponse.error(
                "An unexpected error occurred. Please try again later.",
                traceId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}