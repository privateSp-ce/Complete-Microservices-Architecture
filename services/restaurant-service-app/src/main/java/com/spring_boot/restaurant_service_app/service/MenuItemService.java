package com.spring_boot.restaurant_service_app.service;

import com.spring_boot.restaurant_service_app.dto.request.MenuItemRequest;
import com.spring_boot.restaurant_service_app.dto.response.FullMenuResponse;
import com.spring_boot.restaurant_service_app.dto.response.MenuItemResponse;
import com.spring_boot.restaurant_service_app.entity.enums.DietaryType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for MenuItem business logic
 */
public interface MenuItemService {

    /**
     * Create a new menu item
     */
    MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest request);

    /**
     * Get menu item by ID
     */
    MenuItemResponse getMenuItemById(Long restaurantId, Long itemId);

    /**
     * Update menu item
     */
    MenuItemResponse updateMenuItem(Long restaurantId, Long itemId, MenuItemRequest request);

    /**
     * Delete menu item
     */
    void deleteMenuItem(Long restaurantId, Long itemId);

    /**
     * Get all menu items for a restaurant
     */
    List<MenuItemResponse> getMenuItemsByRestaurant(Long restaurantId);

    /**
     * Get menu items by category
     */
    List<MenuItemResponse> getMenuItemsByCategory(Long restaurantId, Long categoryId);

    /**
     * Get full menu with categories
     */
    FullMenuResponse getFullMenu(Long restaurantId);

    /**
     * Search menu items by name
     */
    List<MenuItemResponse> searchMenuItems(Long restaurantId, String name);

    /**
     * Get menu items by dietary type
     */
    List<MenuItemResponse> getMenuItemsByDietaryType(Long restaurantId, DietaryType dietaryType);

    /**
     * Get bestseller items
     */
    List<MenuItemResponse> getBestsellerItems(Long restaurantId);

    /**
     * Get discounted items
     */
    List<MenuItemResponse> getDiscountedItems(Long restaurantId);

    /**
     * Get items within price range
     */
    List<MenuItemResponse> getItemsByPriceRange(Long restaurantId, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Toggle item availability
     */
    MenuItemResponse toggleAvailability(Long restaurantId, Long itemId);
}