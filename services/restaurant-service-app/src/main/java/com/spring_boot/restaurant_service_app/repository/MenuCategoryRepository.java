package com.spring_boot.restaurant_service_app.repository;

import com.spring_boot.restaurant_service_app.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for MenuCategory entity
 * Provides database operations for menu categories
 */
@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

    /**
     * Find all categories by restaurant ID
     */
    List<MenuCategory> findByRestaurantId(Long restaurantId);

    /**
     * Find all active categories by restaurant ID
     */
    List<MenuCategory> findByRestaurantIdAndIsActiveTrue(Long restaurantId);

    /**
     * Find all active categories by restaurant ID, ordered by display order
     */
    List<MenuCategory> findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(Long restaurantId);

    /**
     * Find category by ID and restaurant ID
     */
    Optional<MenuCategory> findByIdAndRestaurantId(Long id, Long restaurantId);

    /**
     * Find category by name and restaurant ID
     */
    Optional<MenuCategory> findByNameAndRestaurantId(String name, Long restaurantId);

    /**
     * Check if category exists by name and restaurant ID
     */
    boolean existsByNameAndRestaurantId(String name, Long restaurantId);

    /**
     * Count categories by restaurant ID
     */
    long countByRestaurantId(Long restaurantId);

    /**
     * Count active categories by restaurant ID
     */
    long countByRestaurantIdAndIsActiveTrue(Long restaurantId);

    /**
     * Delete all categories by restaurant ID
     */
    void deleteByRestaurantId(Long restaurantId);
}