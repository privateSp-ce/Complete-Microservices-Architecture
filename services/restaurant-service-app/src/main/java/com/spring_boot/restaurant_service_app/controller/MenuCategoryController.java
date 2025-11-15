package com.spring_boot.restaurant_service_app.controller;

import com.spring_boot.restaurant_service_app.dto.request.MenuCategoryRequest;
import com.spring_boot.restaurant_service_app.dto.common.ApiResponse;
import com.spring_boot.restaurant_service_app.dto.response.MenuCategoryResponse;
import com.spring_boot.restaurant_service_app.service.MenuCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/categories")
@RequiredArgsConstructor
@Slf4j
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;

    /**
     * Create a new menu category
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MenuCategoryResponse>> createCategory(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuCategoryRequest request) {
        log.info("REST request to create category for restaurant ID: {}", restaurantId);
        MenuCategoryResponse response = menuCategoryService.createCategory(restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", response));
    }

    /**
     * Get category by ID
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<MenuCategoryResponse>> getCategoryById(
            @PathVariable Long restaurantId,
            @PathVariable Long categoryId) {
        log.info("REST request to get category ID: {} for restaurant ID: {}", categoryId, restaurantId);
        MenuCategoryResponse response = menuCategoryService.getCategoryById(restaurantId, categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", response));
    }

    /**
     * Update category
     */
    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<MenuCategoryResponse>> updateCategory(
            @PathVariable Long restaurantId,
            @PathVariable Long categoryId,
            @Valid @RequestBody MenuCategoryRequest request) {
        log.info("REST request to update category ID: {} for restaurant ID: {}", categoryId, restaurantId);
        MenuCategoryResponse response = menuCategoryService.updateCategory(restaurantId, categoryId, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }

    /**
     * Delete category
     */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long restaurantId,
            @PathVariable Long categoryId) {
        log.info("REST request to delete category ID: {} for restaurant ID: {}", categoryId, restaurantId);
        menuCategoryService.deleteCategory(restaurantId, categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }

    /**
     * Get all categories for a restaurant
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuCategoryResponse>>> getCategoriesByRestaurant(
            @PathVariable Long restaurantId) {
        log.info("REST request to get all categories for restaurant ID: {}", restaurantId);
        List<MenuCategoryResponse> response = menuCategoryService.getCategoriesByRestaurant(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", response));
    }

    /**
     * Get active categories for a restaurant
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<MenuCategoryResponse>>> getActiveCategoriesByRestaurant(
            @PathVariable Long restaurantId) {
        log.info("REST request to get active categories for restaurant ID: {}", restaurantId);
        List<MenuCategoryResponse> response = menuCategoryService.getActiveCategoriesByRestaurant(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Active categories retrieved successfully", response));
    }

    /**
     * Reorder categories
     */
    @PutMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderCategories(
            @PathVariable Long restaurantId,
            @RequestBody List<Long> categoryIds) {
        log.info("REST request to reorder categories for restaurant ID: {}", restaurantId);
        menuCategoryService.reorderCategories(restaurantId, categoryIds);
        return ResponseEntity.ok(ApiResponse.success("Categories reordered successfully"));
    }
}