package com.spring_boot.restaurant_service_app.repository;

import com.spring_boot.restaurant_service_app.entity.MenuItem;
import com.spring_boot.restaurant_service_app.entity.enums.DietaryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for MenuItem entity
 * Provides database operations for menu items
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /**
     * Find all menu items by restaurant ID
     */
    List<MenuItem> findByRestaurantId(Long restaurantId);

    /**
     * Find all available menu items by restaurant ID
     */
    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);

    /**
     * Find menu items by category ID
     */
    List<MenuItem> findByCategoryId(Long categoryId);

    /**
     * Find available menu items by category ID
     */
    List<MenuItem> findByCategoryIdAndIsAvailableTrue(Long categoryId);

    /**
     * Find menu item by ID and restaurant ID
     */
    Optional<MenuItem> findByIdAndRestaurantId(Long id, Long restaurantId);

    /**
     * Find menu items by restaurant ID and category ID
     */
    List<MenuItem> findByRestaurantIdAndCategoryId(Long restaurantId, Long categoryId);

    /**
     * Find available menu items by restaurant ID and category ID
     */
    List<MenuItem> findByRestaurantIdAndCategoryIdAndIsAvailableTrue(Long restaurantId, Long categoryId);

    /**
     * Find bestseller items by restaurant ID
     */
    List<MenuItem> findByRestaurantIdAndIsBestsellerTrueAndIsAvailableTrue(Long restaurantId);

    /**
     * Find menu items by dietary type
     */
    List<MenuItem> findByRestaurantIdAndDietaryTypeAndIsAvailableTrue(
            Long restaurantId,
            DietaryType dietaryType
    );

    /**
     * Find vegetarian items
     */
    List<MenuItem> findByRestaurantIdAndDietaryTypeAndIsAvailableTrue(Long restaurantId);

    /**
     * Search menu items by name
     */
    @Query("SELECT m FROM MenuItem m WHERE " +
            "m.restaurantId = :restaurantId AND " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')) AND " +
            "m.isAvailable = true")
    List<MenuItem> searchByName(@Param("restaurantId") Long restaurantId, @Param("name") String name);

    /**
     * Find menu items within price range
     */
    @Query("SELECT m FROM MenuItem m WHERE " +
            "m.restaurantId = :restaurantId AND " +
            "m.price BETWEEN :minPrice AND :maxPrice AND " +
            "m.isAvailable = true")
    List<MenuItem> findByPriceRange(
            @Param("restaurantId") Long restaurantId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    /**
     * Find items on discount
     */
    @Query("SELECT m FROM MenuItem m WHERE " +
            "m.restaurantId = :restaurantId AND " +
            "m.discountedPrice IS NOT NULL AND " +
            "m.discountedPrice < m.price AND " +
            "m.isAvailable = true")
    List<MenuItem> findDiscountedItems(@Param("restaurantId") Long restaurantId);

    /**
     * Find top rated items by restaurant
     */
    @Query("SELECT m FROM MenuItem m WHERE " +
            "m.restaurantId = :restaurantId AND " +
            "m.averageRating >= :minRating AND " +
            "m.isAvailable = true " +
            "ORDER BY m.averageRating DESC")
    List<MenuItem> findTopRatedItems(
            @Param("restaurantId") Long restaurantId,
            @Param("minRating") Double minRating
    );

    /**
     * Count menu items by restaurant ID
     */
    long countByRestaurantId(Long restaurantId);

    /**
     * Count available menu items by restaurant ID
     */
    long countByRestaurantIdAndIsAvailableTrue(Long restaurantId);

    /**
     * Count menu items by category ID
     */
    long countByCategoryId(Long categoryId);

    /**
     * Check if menu item exists by name and restaurant ID
     */
    boolean existsByNameAndRestaurantId(String name, Long restaurantId);

    /**
     * Delete all menu items by restaurant ID
     */
    void deleteByRestaurantId(Long restaurantId);

    /**
     * Delete all menu items by category ID
     */
    void deleteByCategoryId(Long categoryId);
}