package com.spring_boot.restaurant_service_app.repository;

import com.spring_boot.restaurant_service_app.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Restaurant entity
 * Provides database operations for restaurants
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     * Find restaurant by ID and active status
     */
    Optional<Restaurant> findByIdAndIsActiveTrue(Long id);

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
    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%')) AND r.isActive = true")
    List<Restaurant> searchByName(@Param("name") String name);

    /**
     * Find restaurants by cuisine type
     */
    @Query("SELECT r FROM Restaurant r WHERE r.cuisineTypes LIKE %:cuisineType% AND r.isActive = true")
    List<Restaurant> findByCuisineType(@Param("cuisineType") String cuisineType);

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
    @Query("SELECT r FROM Restaurant r WHERE r.averageRating >= :minRating AND r.isActive = true ORDER BY r.averageRating DESC")
    List<Restaurant> findByMinimumRating(@Param("minRating") Double minRating);

    /**
     * Find top rated restaurants in a city
     */
    @Query("SELECT r FROM Restaurant r WHERE r.city = :city AND r.isActive = true AND r.isVerified = true ORDER BY r.averageRating DESC")
    List<Restaurant> findTopRatedInCity(@Param("city") String city);

    /**
     * Find restaurants near location (using latitude/longitude)
     * Note: This is a simple bounding box search
     * For production, consider using spatial queries or geohash
     */
    @Query("SELECT r FROM Restaurant r WHERE " +
            "r.latitude BETWEEN :minLat AND :maxLat AND " +
            "r.longitude BETWEEN :minLng AND :maxLng AND " +
            "r.isActive = true AND r.isVerified = true")
    List<Restaurant> findNearby(
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLng") Double minLng,
            @Param("maxLng") Double maxLng
    );
}