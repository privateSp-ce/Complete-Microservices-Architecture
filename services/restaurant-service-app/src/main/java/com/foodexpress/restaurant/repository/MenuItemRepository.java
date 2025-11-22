package com.foodexpress.restaurant.repository;

import com.foodexpress.restaurant.entity.MenuItem;
import com.foodexpress.restaurant.entity.enums.DietaryType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for MenuItem entity
 * Provides database operations for menu items
 */
@Repository
public interface MenuItemRepository extends MongoRepository<MenuItem, String> {

    /**
     * Find all menu items by restaurant ID
     */
    List<MenuItem> findByRestaurantId(String restaurantId);

    /**
     * Find all available menu items by restaurant ID
     */
    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(String restaurantId);

    /**
     * Find menu items by category ID
     */
    List<MenuItem> findByCategoryId(String categoryId);

    /**
     * Find available menu items by category ID
     */
    List<MenuItem> findByCategoryIdAndIsAvailableTrue(String categoryId);

    /**
     * Find menu item by ID and restaurant ID
     */
    Optional<MenuItem> findByIdAndRestaurantId(String id, String restaurantId);

    /**
     * Find menu items by restaurant ID and category ID
     */
    List<MenuItem> findByRestaurantIdAndCategoryId(String restaurantId, String categoryId);

    /**
     * Find available menu items by restaurant ID and category ID
     */
    List<MenuItem> findByRestaurantIdAndCategoryIdAndIsAvailableTrue(String restaurantId, String categoryId);

    /**
     * Find bestseller items by restaurant ID
     */
    List<MenuItem> findByRestaurantIdAndIsBestsellerTrueAndIsAvailableTrue(String restaurantId);

    /**
     * Find menu items by dietary type
     */
    List<MenuItem> findByRestaurantIdAndDietaryTypeAndIsAvailableTrue(
            String restaurantId,
            DietaryType dietaryType
    );

    /**
     * Search menu items by name
     */
    @Query("{ 'restaurantId': ?0, 'name': { $regex: ?1, $options: 'i' }, 'isAvailable': true }")
    List<MenuItem> searchByName(String restaurantId, String name);

    /**
     * Find menu items within price range
     */
    @Query("{ 'restaurantId': ?0, 'price': { $gte: ?1, $lte: ?2 }, 'isAvailable': true }")
    List<MenuItem> findByPriceRange(
            String restaurantId,
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

    /**
     * Find items on discount
     * (MongoDB doesn't easily compare two fields in a simple query without aggregation, but we can do a simpler check for existence or non-null)
     * For strictly 'discountedPrice < price', we need $expr.
     */
    @Query("{ 'restaurantId': ?0, 'isAvailable': true, $expr: { $lt: ['$discountedPrice', '$price'] } }")
    List<MenuItem> findDiscountedItems(String restaurantId);

    /**
     * Find top rated items by restaurant
     */
    @Query(value = "{ 'restaurantId': ?0, 'averageRating': { $gte: ?1 }, 'isAvailable': true }", sort = "{ 'averageRating': -1 }")
    List<MenuItem> findTopRatedItems(
            String restaurantId,
            Double minRating
    );

    /**
     * Count menu items by restaurant ID
     */
    long countByRestaurantId(String restaurantId);

    /**
     * Count available menu items by restaurant ID
     */
    long countByRestaurantIdAndIsAvailableTrue(String restaurantId);

    /**
     * Count menu items by category ID
     */
    long countByCategoryId(String categoryId);

    /**
     * Check if menu item exists by name and restaurant ID
     */
    boolean existsByNameAndRestaurantId(String name, String restaurantId);

    /**
     * Delete all menu items by restaurant ID
     */
    void deleteByRestaurantId(String restaurantId);

    /**
     * Delete all menu items by category ID
     */
    void deleteByCategoryId(String categoryId);
}
