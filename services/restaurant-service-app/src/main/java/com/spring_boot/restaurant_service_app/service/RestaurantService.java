package com.spring_boot.restaurant_service_app.service;

import com.spring_boot.restaurant_service_app.dto.common.PageResponse;
import com.spring_boot.restaurant_service_app.dto.request.RestaurantRegistrationRequest;
import com.spring_boot.restaurant_service_app.dto.request.RestaurantSearchRequest;
import com.spring_boot.restaurant_service_app.dto.request.RestaurantUpdateRequest;
import com.spring_boot.restaurant_service_app.dto.response.RestaurantResponse;
import com.spring_boot.restaurant_service_app.dto.response.RestaurantSummaryResponse;

import java.util.List;

/**
 * Service interface for Restaurant business logic
 */
public interface RestaurantService {

    /**
     * Register a new restaurant
     */
    RestaurantResponse registerRestaurant(RestaurantRegistrationRequest request);

    /**
     * Get restaurant by ID
     */
    RestaurantResponse getRestaurantById(Long id);

    /**
     * Update restaurant details
     */
    RestaurantResponse updateRestaurant(Long id, RestaurantUpdateRequest request);

    /**
     * Delete restaurant (soft delete)
     */
    void deleteRestaurant(Long id);

    /**
     * Get all restaurants by owner
     */
    List<RestaurantResponse> getRestaurantsByOwner(Long ownerUserId);

    /**
     * Search restaurants with filters
     */
    PageResponse<RestaurantSummaryResponse> searchRestaurants(RestaurantSearchRequest request);

    /**
     * Get restaurants by city
     */
    List<RestaurantSummaryResponse> getRestaurantsByCity(String city);

    /**
     * Get nearby restaurants
     */
    List<RestaurantSummaryResponse> getNearbyRestaurants(Double latitude, Double longitude, Double radiusKm);

    /**
     * Verify restaurant (admin only)
     */
    RestaurantResponse verifyRestaurant(Long id);

    /**
     * Toggle restaurant accepting orders status
     */
    RestaurantResponse toggleAcceptingOrders(Long id);
}