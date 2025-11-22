package com.foodexpress.restaurant.service.impl;

import com.foodexpress.restaurant.dto.request.MenuCategoryRequest;
import com.foodexpress.restaurant.dto.response.MenuCategoryResponse;
import com.foodexpress.restaurant.entity.MenuCategory;
import com.foodexpress.restaurant.entity.Restaurant;
import com.foodexpress.restaurant.exception.DuplicateResourceException;
import com.foodexpress.restaurant.exception.ResourceNotFoundException;
import com.foodexpress.restaurant.repository.MenuCategoryRepository;
import com.foodexpress.restaurant.repository.MenuItemRepository;
import com.foodexpress.restaurant.repository.RestaurantRepository;
import com.foodexpress.restaurant.service.MenuCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuCategoryServiceImpl implements MenuCategoryService {

    private final MenuCategoryRepository menuCategoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    @Override
    public MenuCategoryResponse createCategory(String restaurantId, MenuCategoryRequest request) {
        log.info("Creating menu category for restaurant ID: {}", restaurantId);

        // Verify restaurant exists
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        // Check if category name already exists for this restaurant
        if (menuCategoryRepository.existsByNameAndRestaurantId(request.getName(), restaurantId)) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists for this restaurant");
        }

        // Create category
        MenuCategory category = MenuCategory.builder()
                .restaurantId(restaurantId)
                .name(request.getName())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        MenuCategory savedCategory = menuCategoryRepository.save(category);
        log.info("Menu category created successfully with ID: {}", savedCategory.getId());

        return mapToCategoryResponse(savedCategory);
    }

    @Override
    public MenuCategoryResponse getCategoryById(String restaurantId, String categoryId) {
        log.info("Fetching category ID: {} for restaurant ID: {}", categoryId, restaurantId);

        MenuCategory category = menuCategoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        return mapToCategoryResponse(category);
    }

    @Override
    public MenuCategoryResponse updateCategory(String restaurantId, String categoryId, MenuCategoryRequest request) {
        log.info("Updating category ID: {} for restaurant ID: {}", categoryId, restaurantId);

        MenuCategory category = menuCategoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        // Check if new name conflicts with existing category
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (menuCategoryRepository.existsByNameAndRestaurantId(request.getName(), restaurantId)) {
                throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists for this restaurant");
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        MenuCategory updatedCategory = menuCategoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", updatedCategory.getId());

        return mapToCategoryResponse(updatedCategory);
    }

    @Override
    public void deleteCategory(String restaurantId, String categoryId) {
        log.info("Deleting category ID: {} for restaurant ID: {}", categoryId, restaurantId);

        MenuCategory category = menuCategoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        menuCategoryRepository.delete(category);
        log.info("Category deleted successfully with ID: {}", categoryId);
    }

    @Override
    public List<MenuCategoryResponse> getCategoriesByRestaurant(String restaurantId) {
        log.info("Fetching all categories for restaurant ID: {}", restaurantId);

        List<MenuCategory> categories = menuCategoryRepository.findByRestaurantId(restaurantId);
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MenuCategoryResponse> getActiveCategoriesByRestaurant(String restaurantId) {
        log.info("Fetching active categories for restaurant ID: {}", restaurantId);

        List<MenuCategory> categories = menuCategoryRepository.findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(restaurantId);
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void reorderCategories(String restaurantId, List<String> categoryIds) {
        log.info("Reordering categories for restaurant ID: {}", restaurantId);

        for (int i = 0; i < categoryIds.size(); i++) {
            String categoryId = categoryIds.get(i);
            MenuCategory category = menuCategoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
            category.setDisplayOrder(i);
            menuCategoryRepository.save(category);
        }

        log.info("Categories reordered successfully for restaurant ID: {}", restaurantId);
    }

    // Helper method
    private MenuCategoryResponse mapToCategoryResponse(MenuCategory category) {
        long itemCount = menuItemRepository.countByCategoryId(category.getId());

        return MenuCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .itemCount((int) itemCount)
                .build();
    }
}
