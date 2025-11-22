package com.foodexpress.restaurant.repository;

import com.foodexpress.restaurant.entity.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Restaurant entity
 * Provides database operations for restaurants
 */
@Repository
public interface RestaurantRepository extends MongoRepository<Restaurant, String> {

    /**
     * Find restaurant by ID and active status
     */
    Optional<Restaurant> findByIdAndIsActiveTrue(String id);

    /**
     * Find all restaurants by owner user ID
     */
    List<Restaurant> findByOwnerUserId(Long ownerUserId);

    /**
     * Find all restaurants by owner user ID and active status
     */
    List<Restaurant> findByOwnerUserIdAndIsActiveTrue(Long ownerUserId);

    /**
     * Find all active restaurants
     */
    List<Restaurant> findByIsActiveTrue();

    /**
     * Find all verified and active restaurants
     */
    List<Restaurant> findByIsVerifiedTrueAndIsActiveTrue();

    /**
     * Find restaurants by city
     */
    List<Restaurant> findByCityAndIsActiveTrue(String city);

    /**
     * Find restaurants by city and verified status
     */
    List<Restaurant> findByCityAndIsVerifiedTrueAndIsActiveTrue(String city);

    /**
     * Search restaurants by name (case-insensitive)
     */
    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': true }")
    List<Restaurant> searchByName(String name);

    /**
     * Find restaurants by cuisine type
     */
    @Query("{ 'cuisineTypes': { $regex: ?0, $options: 'i' }, 'isActive': true }")
    List<Restaurant> findByCuisineType(String cuisineType);

    /**
     * Find restaurants accepting orders
     */
    List<Restaurant> findByIsAcceptingOrdersTrueAndIsActiveTrueAndIsVerifiedTrue();

    /**
     * Check if restaurant exists by owner user ID
     */
    boolean existsByOwnerUserId(Long ownerUserId);

    /**
     * Count restaurants by owner user ID
     */
    long countByOwnerUserId(Long ownerUserId);

    /**
     * Find restaurants with rating above threshold
     */
    @Query("{ 'averageRating': { $gte: ?0 }, 'isActive': true }")
    List<Restaurant> findByMinimumRating(Double minRating);

    /**
     * Find top rated restaurants in a city
     * MongoRepository sort is usually done via method name or Pageable,
     * but @Query doesn't support 'ORDER BY'. We rely on method naming or client-side sort for complex custom queries.
     * Or simpler:
     */
    List<Restaurant> findByCityAndIsActiveTrueAndIsVerifiedTrueOrderByAverageRatingDesc(String city);

    /**
     * Find restaurants near location
     * Using strict bounding box in Mongo query syntax
     */
    @Query("{ 'latitude': { $gte: ?0, $lte: ?1 }, 'longitude': { $gte: ?2, $lte: ?3 }, 'isActive': true, 'isVerified': true }")
    List<Restaurant> findNearby(
            Double minLat,
            Double maxLat,
            Double minLng,
            Double maxLng
    );
}
