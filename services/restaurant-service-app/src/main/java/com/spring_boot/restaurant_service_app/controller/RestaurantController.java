package com.spring_boot.restaurant_service_app.controller;

import com.spring_boot.restaurant_service_app.dto.common.PageResponse;
import com.spring_boot.restaurant_service_app.dto.request.RestaurantRegistrationRequest;
import com.spring_boot.restaurant_service_app.dto.request.RestaurantSearchRequest;
import com.spring_boot.restaurant_service_app.dto.request.RestaurantUpdateRequest;
import com.spring_boot.restaurant_service_app.dto.common.ApiResponse;
import com.spring_boot.restaurant_service_app.dto.response.RestaurantResponse;
import com.spring_boot.restaurant_service_app.dto.response.RestaurantSummaryResponse;
import com.spring_boot.restaurant_service_app.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Slf4j
public class RestaurantController {

    private final RestaurantService restaurantService;

    /**
     * Register a new restaurant
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantResponse>> registerRestaurant(
            @Valid @RequestBody RestaurantRegistrationRequest request) {
        log.info("REST request to register restaurant: {}", request.getName());
        RestaurantResponse response = restaurantService.registerRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Restaurant registered successfully", response));
    }

    /**
     * Get restaurant by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getRestaurantById(@PathVariable Long id) {
        log.info("REST request to get restaurant with ID: {}", id);
        RestaurantResponse response = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(ApiResponse.success("Restaurant retrieved successfully", response));
    }

    /**
     * Update restaurant details
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantUpdateRequest request) {
        log.info("REST request to update restaurant with ID: {}", id);
        RestaurantResponse response = restaurantService.updateRestaurant(id, request);
        return ResponseEntity.ok(ApiResponse.success("Restaurant updated successfully", response));
    }

    /**
     * Delete restaurant (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(@PathVariable Long id) {
        log.info("REST request to delete restaurant with ID: {}", id);
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok(ApiResponse.success("Restaurant deleted successfully"));
    }

    /**
     * Get restaurants by owner
     */
    @GetMapping("/owner/{ownerUserId}")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getRestaurantsByOwner(
            @PathVariable Long ownerUserId) {
        log.info("REST request to get restaurants for owner: {}", ownerUserId);
        List<RestaurantResponse> response = restaurantService.getRestaurantsByOwner(ownerUserId);
        return ResponseEntity.ok(ApiResponse.success("Restaurants retrieved successfully", response));
    }

    /**
     * Search restaurants with filters and pagination
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<RestaurantSummaryResponse>>> searchRestaurants(
            @RequestBody RestaurantSearchRequest request) {
        log.info("REST request to search restaurants");
        PageResponse<RestaurantSummaryResponse> response = restaurantService.searchRestaurants(request);
        return ResponseEntity.ok(ApiResponse.success("Restaurants retrieved successfully", response));
    }

    /**
     * Get restaurants by city
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<ApiResponse<List<RestaurantSummaryResponse>>> getRestaurantsByCity(
            @PathVariable String city) {
        log.info("REST request to get restaurants in city: {}", city);
        List<RestaurantSummaryResponse> response = restaurantService.getRestaurantsByCity(city);
        return ResponseEntity.ok(ApiResponse.success("Restaurants retrieved successfully", response));
    }

    /**
     * Get nearby restaurants
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<RestaurantSummaryResponse>>> getNearbyRestaurants(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {
        log.info("REST request to get nearby restaurants at {}, {} within {} km", latitude, longitude, radiusKm);
        List<RestaurantSummaryResponse> response = restaurantService.getNearbyRestaurants(latitude, longitude, radiusKm);
        return ResponseEntity.ok(ApiResponse.success("Nearby restaurants retrieved successfully", response));
    }

    /**
     * Verify restaurant (admin only)
     */
    @PutMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<RestaurantResponse>> verifyRestaurant(@PathVariable Long id) {
        log.info("REST request to verify restaurant with ID: {}", id);
        RestaurantResponse response = restaurantService.verifyRestaurant(id);
        return ResponseEntity.ok(ApiResponse.success("Restaurant verified successfully", response));
    }

    /**
     * Toggle accepting orders status
     */
    @PutMapping("/{id}/toggle-orders")
    public ResponseEntity<ApiResponse<RestaurantResponse>> toggleAcceptingOrders(@PathVariable Long id) {
        log.info("REST request to toggle accepting orders for restaurant ID: {}", id);
        RestaurantResponse response = restaurantService.toggleAcceptingOrders(id);
        return ResponseEntity.ok(ApiResponse.success("Restaurant status updated successfully", response));
    }
}