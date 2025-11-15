package com.spring_boot.restaurant_service_app.controller;

import com.spring_boot.restaurant_service_app.dto.request.MenuItemRequest;
import com.spring_boot.restaurant_service_app.dto.response.ApiResponse;
import com.spring_boot.restaurant_service_app.dto.response.FullMenuResponse;
import com.spring_boot.restaurant_service_app.dto.response.MenuItemResponse;
import com.spring_boot.restaurant_service_app.entity.enums.DietaryType;
import com.spring_boot.restaurant_service_app.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/menu-items")
@RequiredArgsConstructor
@Slf4j
public class MenuItemController {

    private final MenuItemService menuItemService;

    /**
     * Create a new menu item
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemResponse>> createMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {
        log.info("REST request to create menu item for restaurant ID: {}", restaurantId);
        MenuItemResponse response = menuItemService.createMenuItem(restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu item created successfully", response));
    }

    /**
     * Get menu item by ID
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getMenuItemById(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId) {
        log.info("REST request to get menu item ID: {} for restaurant ID: {}", itemId, restaurantId);
        MenuItemResponse response = menuItemService.getMenuItemById(restaurantId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Menu item retrieved successfully", response));
    }

    /**
     * Update menu item
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request) {
        log.info("REST request to update menu item ID: {} for restaurant ID: {}", itemId, restaurantId);
        MenuItemResponse response = menuItemService.updateMenuItem(restaurantId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Menu item updated successfully", response));
    }

    /**
     * Delete menu item
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId) {
        log.info("REST request to delete menu item ID: {} for restaurant ID: {}", itemId, restaurantId);
        menuItemService.deleteMenuItem(restaurantId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Menu item deleted successfully"));
    }

    /**
     * Get all menu items for a restaurant
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenuItemsByRestaurant(
            @PathVariable Long restaurantId) {
        log.info("REST request to get all menu items for restaurant ID: {}", restaurantId);
        List<MenuItemResponse> response = menuItemService.getMenuItemsByRestaurant(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Menu items retrieved successfully", response));
    }

    /**
     * Get menu items by category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenuItemsByCategory(
            @PathVariable Long restaurantId,
            @PathVariable Long categoryId) {
        log.info("REST request to get menu items for restaurant ID: {} and category ID: {}", restaurantId, categoryId);
        List<MenuItemResponse> response = menuItemService.getMenuItemsByCategory(restaurantId, categoryId);
        return ResponseEntity.ok(ApiResponse.success("Menu items retrieved successfully", response));
    }

    /**
     * Get full menu with categories
     */
    @GetMapping("/full-menu")
    public ResponseEntity<ApiResponse<FullMenuResponse>> getFullMenu(@PathVariable Long restaurantId) {
        log.info("REST request to get full menu for restaurant ID: {}", restaurantId);
        FullMenuResponse response = menuItemService.getFullMenu(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Full menu retrieved successfully", response));
    }

    /**
     * Search menu items by name
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> searchMenuItems(
            @PathVariable Long restaurantId,
            @RequestParam String name) {
        log.info("REST request to search menu items for restaurant ID: {} with name: {}", restaurantId, name);
        List<MenuItemResponse> response = menuItemService.searchMenuItems(restaurantId, name);
        return ResponseEntity.ok(ApiResponse.success("Menu items retrieved successfully", response));
    }

    /**
     * Get menu items by dietary type
     */
    @GetMapping("/dietary/{dietaryType}")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenuItemsByDietaryType(
            @PathVariable Long restaurantId,
            @PathVariable DietaryType dietaryType) {
        log.info("REST request to get menu items for restaurant ID: {} with dietary type: {}", restaurantId, dietaryType);
        List<MenuItemResponse> response = menuItemService.getMenuItemsByDietaryType(restaurantId, dietaryType);
        return ResponseEntity.ok(ApiResponse.success("Menu items retrieved successfully", response));
    }

    /**
     * Get bestseller items
     */
    @GetMapping("/bestsellers")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getBestsellerItems(@PathVariable Long restaurantId) {
        log.info("REST request to get bestseller items for restaurant ID: {}", restaurantId);
        List<MenuItemResponse> response = menuItemService.getBestsellerItems(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Bestseller items retrieved successfully", response));
    }

    /**
     * Get discounted items
     */
    @GetMapping("/discounted")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getDiscountedItems(@PathVariable Long restaurantId) {
        log.info("REST request to get discounted items for restaurant ID: {}", restaurantId);
        List<MenuItemResponse> response = menuItemService.getDiscountedItems(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Discounted items retrieved successfully", response));
    }

    /**
     * Get items within price range
     */
    @GetMapping("/price-range")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getItemsByPriceRange(
            @PathVariable Long restaurantId,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        log.info("REST request to get items for restaurant ID: {} within price range {} - {}", restaurantId, minPrice, maxPrice);
        List<MenuItemResponse> response = menuItemService.getItemsByPriceRange(restaurantId, minPrice, maxPrice);
        return ResponseEntity.ok(ApiResponse.success("Menu items retrieved successfully", response));
    }

    /**
     * Toggle item availability
     */
    @PutMapping("/{itemId}/toggle-availability")
    public ResponseEntity<ApiResponse<MenuItemResponse>> toggleAvailability(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId) {
        log.info("REST request to toggle availability for item ID: {} in restaurant ID: {}", itemId, restaurantId);
        MenuItemResponse response = menuItemService.toggleAvailability(restaurantId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item availability updated successfully", response));
    }
}