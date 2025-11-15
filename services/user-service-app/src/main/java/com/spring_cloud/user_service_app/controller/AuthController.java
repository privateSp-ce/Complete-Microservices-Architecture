package com.spring_cloud.user_service_app.controller;

import com.spring_cloud.user_service_app.dto.ApiResponse;
import com.spring_cloud.user_service_app.dto.request.UserLoginRequest;
import com.spring_cloud.user_service_app.dto.request.UserRegistrationRequest;
import com.spring_cloud.user_service_app.dto.response.AuthResponse;
import com.spring_cloud.user_service_app.dto.response.UserResponse;
import com.spring_cloud.user_service_app.service.AuthService;
import io.micrometer.tracing.Tracer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints
 * Handles user registration and login
 */
@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final Tracer tracer;

    // Constructor with Optional Tracer
    public AuthController(AuthService authService,
                          @Autowired(required = false) Tracer tracer) {
        this.authService = authService;
        this.tracer = tracer;
    }

    /**
     * Get current trace ID
     */
    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "N/A";
    }

    /**
     * Register a new user
     * POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody UserRegistrationRequest request
    ) {
        String traceId = getTraceId();
        log.info("Registration request received for email: {}, TraceId: {}", request.getEmail(), traceId);

        UserResponse user = authService.register(request);

        ApiResponse<UserResponse> response = ApiResponse.success(
                user,
                "User registered successfully",
                traceId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user and get JWT token
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody UserLoginRequest request
    ) {
        String traceId = getTraceId();
        log.info("Login request received for email: {}, TraceId: {}", request.getEmail(), traceId);

        AuthResponse authResponse = authService.login(request);

        ApiResponse<AuthResponse> response = ApiResponse.success(
                authResponse,
                "Login successful",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Check if email is available
     * GET /api/v1/auth/check-email?email=user@example.com
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(
            @RequestParam String email
    ) {
        String traceId = getTraceId();
        log.debug("Checking email availability: {}, TraceId: {}", email, traceId);

        boolean isAvailable = authService.isEmailAvailable(email);

        ApiResponse<Boolean> response = ApiResponse.success(
                isAvailable,
                isAvailable ? "Email is available" : "Email already exists",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Check if phone is available
     * GET /api/v1/auth/check-phone?phone=9876543210
     */
    @GetMapping("/check-phone")
    public ResponseEntity<ApiResponse<Boolean>> checkPhoneAvailability(
            @RequestParam String phone
    ) {
        String traceId = getTraceId();
        log.debug("Checking phone availability: {}, TraceId: {}", phone, traceId);

        boolean isAvailable = authService.isPhoneAvailable(phone);

        ApiResponse<Boolean> response = ApiResponse.success(
                isAvailable,
                isAvailable ? "Phone is available" : "Phone already exists",
                traceId
        );

        return ResponseEntity.ok(response);
    }
}