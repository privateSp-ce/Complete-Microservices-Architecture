package com.foodexpress.restaurant.repository;

import com.foodexpress.restaurant.entity.MenuCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for MenuCategory entity
 * Provides database operations for menu categories
 */
@Repository
public interface MenuCategoryRepository extends MongoRepository<MenuCategory, String> {

    /**
     * Find all categories by restaurant ID
     */
    List<MenuCategory> findByRestaurantId(String restaurantId);

    /**
     * Find all active categories by restaurant ID
     */
    List<MenuCategory> findByRestaurantIdAndIsActiveTrue(String restaurantId);

    /**
     * Find all active categories by restaurant ID, ordered by display order
     */
    List<MenuCategory> findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(String restaurantId);

    /**
     * Find category by ID and restaurant ID
     */
    Optional<MenuCategory> findByIdAndRestaurantId(String id, String restaurantId);

    /**
     * Find category by name and restaurant ID
     */
    Optional<MenuCategory> findByNameAndRestaurantId(String name, String restaurantId);

    /**
     * Check if category exists by name and restaurant ID
     */
    boolean existsByNameAndRestaurantId(String name, String restaurantId);

    /**
     * Count categories by restaurant ID
     */
    long countByRestaurantId(String restaurantId);

    /**
     * Count active categories by restaurant ID
     */
    long countByRestaurantIdAndIsActiveTrue(String restaurantId);

    /**
     * Delete all categories by restaurant ID
     */
    void deleteByRestaurantId(String restaurantId);
}
