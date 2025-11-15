package com.spring_boot.restaurant_service_app.service;

import com.spring_boot.restaurant_service_app.dto.request.MenuCategoryRequest;
import com.spring_boot.restaurant_service_app.dto.response.MenuCategoryResponse;

import java.util.List;

/**
 * Service interface for MenuCategory business logic
 */
public interface MenuCategoryService {

    /**
     * Create a new menu category
     */
    MenuCategoryResponse createCategory(Long restaurantId, MenuCategoryRequest request);

    /**
     * Get category by ID
     */
    MenuCategoryResponse getCategoryById(Long restaurantId, Long categoryId);

    /**
     * Update category
     */
    MenuCategoryResponse updateCategory(Long restaurantId, Long categoryId, MenuCategoryRequest request);

    /**
     * Delete category
     */
    void deleteCategory(Long restaurantId, Long categoryId);

    /**
     * Get all categories for a restaurant
     */
    List<MenuCategoryResponse> getCategoriesByRestaurant(Long restaurantId);

    /**
     * Get all active categories for a restaurant (ordered by display order)
     */
    List<MenuCategoryResponse> getActiveCategoriesByRestaurant(Long restaurantId);

    /**
     * Reorder categories
     */
    void reorderCategories(Long restaurantId, List<Long> categoryIds);
}