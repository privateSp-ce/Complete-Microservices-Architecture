package com.foodexpress.restaurant.service;

import com.foodexpress.restaurant.dto.request.MenuItemRequest;
import com.foodexpress.restaurant.dto.response.FullMenuResponse;
import com.foodexpress.restaurant.dto.response.MenuItemResponse;
import com.foodexpress.restaurant.entity.enums.DietaryType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for MenuItem business logic
 */
public interface MenuItemService {

    /**
     * Create a new menu item
     */
    MenuItemResponse createMenuItem(String restaurantId, MenuItemRequest request);

    /**
     * Get menu item by ID
     */
    MenuItemResponse getMenuItemById(String restaurantId, String itemId);

    /**
     * Update menu item
     */
    MenuItemResponse updateMenuItem(String restaurantId, String itemId, MenuItemRequest request);

    /**
     * Delete menu item
     */
    void deleteMenuItem(String restaurantId, String itemId);

    /**
     * Get all menu items for a restaurant
     */
    List<MenuItemResponse> getMenuItemsByRestaurant(String restaurantId);

    /**
     * Get menu items by category
     */
    List<MenuItemResponse> getMenuItemsByCategory(String restaurantId, String categoryId);

    /**
     * Get full menu with categories
     */
    FullMenuResponse getFullMenu(String restaurantId);

    /**
     * Search menu items by name
     */
    List<MenuItemResponse> searchMenuItems(String restaurantId, String name);

    /**
     * Get menu items by dietary type
     */
    List<MenuItemResponse> getMenuItemsByDietaryType(String restaurantId, DietaryType dietaryType);

    /**
     * Get bestseller items
     */
    List<MenuItemResponse> getBestsellerItems(String restaurantId);

    /**
     * Get discounted items
     */
    List<MenuItemResponse> getDiscountedItems(String restaurantId);

    /**
     * Get items within price range
     */
    List<MenuItemResponse> getItemsByPriceRange(String restaurantId, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Toggle item availability
     */
    MenuItemResponse toggleAvailability(String restaurantId, String itemId);
}
