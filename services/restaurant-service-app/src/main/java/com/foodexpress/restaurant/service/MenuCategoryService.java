package com.foodexpress.restaurant.service;

import com.foodexpress.restaurant.dto.request.MenuCategoryRequest;
import com.foodexpress.restaurant.dto.response.MenuCategoryResponse;

import java.util.List;

/**
 * Service interface for MenuCategory business logic
 */
public interface MenuCategoryService {

    /**
     * Create a new menu category
     */
    MenuCategoryResponse createCategory(String restaurantId, MenuCategoryRequest request);

    /**
     * Get category by ID
     */
    MenuCategoryResponse getCategoryById(String restaurantId, String categoryId);

    /**
     * Update category
     */
    MenuCategoryResponse updateCategory(String restaurantId, String categoryId, MenuCategoryRequest request);

    /**
     * Delete category
     */
    void deleteCategory(String restaurantId, String categoryId);

    /**
     * Get all categories for a restaurant
     */
    List<MenuCategoryResponse> getCategoriesByRestaurant(String restaurantId);

    /**
     * Get all active categories for a restaurant (ordered by display order)
     */
    List<MenuCategoryResponse> getActiveCategoriesByRestaurant(String restaurantId);

    /**
     * Reorder categories
     */
    void reorderCategories(String restaurantId, List<String> categoryIds);
}
