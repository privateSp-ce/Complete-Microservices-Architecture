package com.foodexpress.restaurant.service;

import com.foodexpress.restaurant.dto.common.PageResponse;
import com.foodexpress.restaurant.dto.request.RestaurantRegistrationRequest;
import com.foodexpress.restaurant.dto.request.RestaurantSearchRequest;
import com.foodexpress.restaurant.dto.request.RestaurantUpdateRequest;
import com.foodexpress.restaurant.dto.response.RestaurantResponse;
import com.foodexpress.restaurant.dto.response.RestaurantSummaryResponse;

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
    RestaurantResponse getRestaurantById(String id);

    /**
     * Update restaurant details
     */
    RestaurantResponse updateRestaurant(String id, RestaurantUpdateRequest request);

    /**
     * Delete restaurant (soft delete)
     */
    void deleteRestaurant(String id);

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
    RestaurantResponse verifyRestaurant(String id);

    /**
     * Toggle restaurant accepting orders status
     */
    RestaurantResponse toggleAcceptingOrders(String id);
}
