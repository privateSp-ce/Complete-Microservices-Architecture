package com.foodexpress.user.controller;

import com.foodexpress.user.dto.ApiResponse;
import com.foodexpress.user.dto.request.PasswordChangeRequest;
import com.foodexpress.user.dto.request.UserUpdateRequest;
import com.foodexpress.user.dto.response.UserResponse;
import com.foodexpress.user.service.UserService;
import io.micrometer.tracing.Tracer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user profile management
 * All endpoints require authentication (JWT token)
 */
@RestController
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final Tracer tracer;

    // Constructor with Optional Tracer
    public UserController(UserService userService,
                          @Autowired(required = false) Tracer tracer) {
        this.userService = userService;
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
     * Get current user profile
     * GET /api/v1/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile() {
        String traceId = getTraceId();
        log.info("Get current user profile request, TraceId: {}", traceId);

        UserResponse user = userService.getCurrentUserProfile();

        ApiResponse<UserResponse> response = ApiResponse.success(
                user,
                "User profile fetched successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get current user profile with addresses
     * GET /api/v1/users/me/with-addresses
     */
    @GetMapping("/me/with-addresses")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfileWithAddresses() {
        String traceId = getTraceId();
        log.info("Get current user profile with addresses request, TraceId: {}", traceId);

        UserResponse user = userService.getCurrentUserProfileWithAddresses();

        ApiResponse<UserResponse> response = ApiResponse.success(
                user,
                "User profile with addresses fetched successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Update current user profile
     * PUT /api/v1/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUserProfile(
            @Valid @RequestBody UserUpdateRequest request
    ) {
        String traceId = getTraceId();
        log.info("Update current user profile request, TraceId: {}", traceId);

        UserResponse user = userService.updateCurrentUserProfile(request);

        ApiResponse<UserResponse> response = ApiResponse.success(
                user,
                "Profile updated successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Change password for current user
     * PUT /api/v1/users/me/password
     */
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        String traceId = getTraceId();
        log.info("Change password request, TraceId: {}", traceId);

        userService.changePassword(request);

        ApiResponse<Void> response = ApiResponse.success(
                "Password changed successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Soft delete current user account
     * DELETE /api/v1/users/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteCurrentUser() {
        String traceId = getTraceId();
        log.info("Delete current user request, TraceId: {}", traceId);

        userService.deleteCurrentUser();

        ApiResponse<Void> response = ApiResponse.success(
                "Account deactivated successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Internal endpoint: Get user by ID (for inter-service communication)
     * GET /api/v1/users/internal/{userId}
     */
    @GetMapping("/internal/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        String traceId = getTraceId();
        log.info("Get user by ID request: {}, TraceId: {}", userId, traceId);

        UserResponse user = userService.getUserById(userId);

        ApiResponse<UserResponse> response = ApiResponse.success(
                user,
                "User fetched successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Internal endpoint: Check if user exists
     * GET /api/v1/users/internal/{userId}/exists
     */
    @GetMapping("/internal/{userId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkUserExists(@PathVariable Long userId) {
        String traceId = getTraceId();
        log.debug("Check user exists request: {}, TraceId: {}", userId, traceId);

        boolean exists = userService.existsById(userId);

        ApiResponse<Boolean> response = ApiResponse.success(
                exists,
                exists ? "User exists" : "User not found",
                traceId
        );

        return ResponseEntity.ok(response);
    }
}