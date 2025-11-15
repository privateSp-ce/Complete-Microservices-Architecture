package com.spring_cloud.user_service_app.controller;

import com.spring_cloud.user_service_app.dto.ApiResponse;
import com.spring_cloud.user_service_app.dto.request.AddressCreateRequest;
import com.spring_cloud.user_service_app.dto.request.AddressUpdateRequest;
import com.spring_cloud.user_service_app.dto.response.AddressResponse;
import com.spring_cloud.user_service_app.service.AddressService;
import io.micrometer.tracing.Tracer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for address management
 * All endpoints require authentication (JWT token)
 */
@RestController
@RequestMapping("/api/v1/users/me/addresses")
@Slf4j
public class AddressController {

    private final AddressService addressService;
    private final Tracer tracer;

    // Manual constructor with Optional Tracer
    public AddressController(AddressService addressService,
                             @Autowired(required = false) Tracer tracer) {
        this.addressService = addressService;
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
     * Get all addresses for current user
     * GET /api/v1/users/me/addresses
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAllAddresses() {
        String traceId = getTraceId();
        log.info("Get all addresses request, TraceId: {}", traceId);

        List<AddressResponse> addresses = addressService.getAllAddresses();

        ApiResponse<List<AddressResponse>> response = ApiResponse.success(
                addresses,
                "Addresses fetched successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get address by ID
     * GET /api/v1/users/me/addresses/{addressId}
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(
            @PathVariable Long addressId
    ) {
        String traceId = getTraceId();
        log.info("Get address by ID request: {}, TraceId: {}", addressId, traceId);

        AddressResponse address = addressService.getAddressById(addressId);

        ApiResponse<AddressResponse> response = ApiResponse.success(
                address,
                "Address fetched successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get default address for current user
     * GET /api/v1/users/me/addresses/default
     */
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<AddressResponse>> getDefaultAddress() {
        String traceId = getTraceId();
        log.info("Get default address request, TraceId: {}", traceId);

        AddressResponse address = addressService.getDefaultAddress();

        ApiResponse<AddressResponse> response = ApiResponse.success(
                address,
                "Default address fetched successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Create new address
     * POST /api/v1/users/me/addresses
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @Valid @RequestBody AddressCreateRequest request
    ) {
        String traceId = getTraceId();
        log.info("Create address request, TraceId: {}", traceId);

        AddressResponse address = addressService.createAddress(request);

        ApiResponse<AddressResponse> response = ApiResponse.success(
                address,
                "Address created successfully",
                traceId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update address
     * PUT /api/v1/users/me/addresses/{addressId}
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressUpdateRequest request
    ) {
        String traceId = getTraceId();
        log.info("Update address request: {}, TraceId: {}", addressId, traceId);

        AddressResponse address = addressService.updateAddress(addressId, request);

        ApiResponse<AddressResponse> response = ApiResponse.success(
                address,
                "Address updated successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Set address as default
     * PUT /api/v1/users/me/addresses/{addressId}/set-default
     */
    @PutMapping("/{addressId}/set-default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @PathVariable Long addressId
    ) {
        String traceId = getTraceId();
        log.info("Set default address request: {}, TraceId: {}", addressId, traceId);

        AddressResponse address = addressService.setDefaultAddress(addressId);

        ApiResponse<AddressResponse> response = ApiResponse.success(
                address,
                "Address set as default successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Delete address
     * DELETE /api/v1/users/me/addresses/{addressId}
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long addressId
    ) {
        String traceId = getTraceId();
        log.info("Delete address request: {}, TraceId: {}", addressId, traceId);

        addressService.deleteAddress(addressId);

        ApiResponse<Void> response = ApiResponse.success(
                "Address deleted successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get address count for current user
     * GET /api/v1/users/me/addresses/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getAddressCount() {
        String traceId = getTraceId();
        log.debug("Get address count request, TraceId: {}", traceId);

        long count = addressService.getAddressCount();

        ApiResponse<Long> response = ApiResponse.success(
                count,
                "Address count fetched successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }
}