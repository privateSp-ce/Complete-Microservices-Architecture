package com.spring_boot.restaurant_service_app.service.impl;

import com.spring_boot.restaurant_service_app.dto.request.MenuItemRequest;
import com.spring_boot.restaurant_service_app.dto.response.FullMenuResponse;
import com.spring_boot.restaurant_service_app.dto.response.MenuItemResponse;
import com.spring_boot.restaurant_service_app.entity.MenuCategory;
import com.spring_boot.restaurant_service_app.entity.MenuItem;
import com.spring_boot.restaurant_service_app.entity.Restaurant;
import com.spring_boot.restaurant_service_app.entity.enums.DietaryType;
import com.spring_boot.restaurant_service_app.exception.DuplicateResourceException;
import com.spring_boot.restaurant_service_app.exception.ResourceNotFoundException;
import com.spring_boot.restaurant_service_app.repository.MenuCategoryRepository;
import com.spring_boot.restaurant_service_app.repository.MenuItemRepository;
import com.spring_boot.restaurant_service_app.repository.RestaurantRepository;
import com.spring_boot.restaurant_service_app.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository menuCategoryRepository;

    @Override
    public MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest request) {
        log.info("Creating menu item for restaurant ID: {}", restaurantId);

        // Verify restaurant exists
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        // Verify category exists and belongs to this restaurant
        MenuCategory category = menuCategoryRepository.findByIdAndRestaurantId(request.getCategoryId(), restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        // Check if item name already exists
        if (menuItemRepository.existsByNameAndRestaurantId(request.getName(), restaurantId)) {
            throw new DuplicateResourceException("Menu item with name '" + request.getName() + "' already exists for this restaurant");
        }

        // Create menu item
        MenuItem menuItem = MenuItem.builder()
                .restaurant(restaurant)
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountedPrice(request.getDiscountedPrice())
                .dietaryType(request.getDietaryType())
                .spiceLevel(request.getSpiceLevel())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .isBestseller(request.getIsBestseller() != null ? request.getIsBestseller() : false)
                .imageUrl(request.getImageUrl())
                .preparationTime(request.getPreparationTime())
                .calories(request.getCalories())
                .servesCount(request.getServesCount() != null ? request.getServesCount() : 1)
                .isVegan(request.getIsVegan() != null ? request.getIsVegan() : false)
                .isGlutenFree(request.getIsGlutenFree() != null ? request.getIsGlutenFree() : false)
                .build();

        MenuItem savedItem = menuItemRepository.save(menuItem);
        log.info("Menu item created successfully with ID: {}", savedItem.getId());

        return mapToMenuItemResponse(savedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long restaurantId, Long itemId) {
        log.info("Fetching menu item ID: {} for restaurant ID: {}", itemId, restaurantId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + itemId));

        return mapToMenuItemResponse(menuItem);
    }

    @Override
    public MenuItemResponse updateMenuItem(Long restaurantId, Long itemId, MenuItemRequest request) {
        log.info("Updating menu item ID: {} for restaurant ID: {}", itemId, restaurantId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + itemId));

        // Update category if provided
        if (request.getCategoryId() != null && !request.getCategoryId().equals(menuItem.getCategoryId())) {
            MenuCategory category = menuCategoryRepository.findByIdAndRestaurantId(request.getCategoryId(), restaurantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
            menuItem.setCategory(category);
        }

        // Check if new name conflicts
        if (request.getName() != null && !request.getName().equals(menuItem.getName())) {
            if (menuItemRepository.existsByNameAndRestaurantId(request.getName(), restaurantId)) {
                throw new DuplicateResourceException("Menu item with name '" + request.getName() + "' already exists");
            }
            menuItem.setName(request.getName());
        }

        if (request.getDescription() != null) {
            menuItem.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            menuItem.setPrice(request.getPrice());
        }
        if (request.getDiscountedPrice() != null) {
            menuItem.setDiscountedPrice(request.getDiscountedPrice());
        }
        if (request.getDietaryType() != null) {
            menuItem.setDietaryType(request.getDietaryType());
        }
        if (request.getSpiceLevel() != null) {
            menuItem.setSpiceLevel(request.getSpiceLevel());
        }
        if (request.getIsAvailable() != null) {
            menuItem.setIsAvailable(request.getIsAvailable());
        }
        if (request.getIsBestseller() != null) {
            menuItem.setIsBestseller(request.getIsBestseller());
        }
        if (request.getImageUrl() != null) {
            menuItem.setImageUrl(request.getImageUrl());
        }
        if (request.getPreparationTime() != null) {
            menuItem.setPreparationTime(request.getPreparationTime());
        }
        if (request.getCalories() != null) {
            menuItem.setCalories(request.getCalories());
        }
        if (request.getServesCount() != null) {
            menuItem.setServesCount(request.getServesCount());
        }
        if (request.getIsVegan() != null) {
            menuItem.setIsVegan(request.getIsVegan());
        }
        if (request.getIsGlutenFree() != null) {
            menuItem.setIsGlutenFree(request.getIsGlutenFree());
        }

        MenuItem updatedItem = menuItemRepository.save(menuItem);
        log.info("Menu item updated successfully with ID: {}", updatedItem.getId());

        return mapToMenuItemResponse(updatedItem);
    }

    @Override
    public void deleteMenuItem(Long restaurantId, Long itemId) {
        log.info("Deleting menu item ID: {} for restaurant ID: {}", itemId, restaurantId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + itemId));

        menuItemRepository.delete(menuItem);
        log.info("Menu item deleted successfully with ID: {}", itemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByRestaurant(Long restaurantId) {
        log.info("Fetching all menu items for restaurant ID: {}", restaurantId);

        List<MenuItem> menuItems = menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId);
        return menuItems.stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByCategory(Long restaurantId, Long categoryId) {
        log.info("Fetching menu items for restaurant ID: {} and category ID: {}", restaurantId, categoryId);

        List<MenuItem> menuItems = menuItemRepository.findByRestaurantIdAndCategoryIdAndIsAvailableTrue(restaurantId, categoryId);
        return menuItems.stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FullMenuResponse getFullMenu(Long restaurantId) {
        log.info("Fetching full menu for restaurant ID: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        List<MenuCategory> categories = menuCategoryRepository.findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(restaurantId);

        List<FullMenuResponse.MenuCategoryWithItems> categoryWithItemsList = categories.stream()
                .map(category -> {
                    List<MenuItem> items = menuItemRepository.findByCategoryIdAndIsAvailableTrue(category.getId());
                    List<MenuItemResponse> itemResponses = items.stream()
                            .map(this::mapToMenuItemResponse)
                            .collect(Collectors.toList());

                    return FullMenuResponse.MenuCategoryWithItems.builder()
                            .categoryId(category.getId())
                            .categoryName(category.getName())
                            .categoryDescription(category.getDescription())
                            .displayOrder(category.getDisplayOrder())
                            .items(itemResponses)
                            .build();
                })
                .collect(Collectors.toList());

        return FullMenuResponse.builder()
                .restaurantId(restaurantId)
                .restaurantName(restaurant.getName())
                .categories(categoryWithItemsList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> searchMenuItems(Long restaurantId, String name) {
        log.info("Searching menu items for restaurant ID: {} with name: {}", restaurantId, name);

        List<MenuItem> menuItems = menuItemRepository.searchByName(restaurantId, name);
        return menuItems.stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByDietaryType(Long restaurantId, DietaryType dietaryType) {
        log.info("Fetching menu items for restaurant ID: {} with dietary type: {}", restaurantId, dietaryType);

        List<MenuItem> menuItems = menuItemRepository.findByRestaurantIdAndDietaryTypeAndIsAvailableTrue(restaurantId, dietaryType);
        return menuItems.stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getBestsellerItems(Long restaurantId) {
        log.info("Fetching bestseller items for restaurant ID: {}", restaurantId);

        List<MenuItem> menuItems = menuItemRepository.findByRestaurantIdAndIsBestsellerTrueAndIsAvailableTrue(restaurantId);
        return menuItems.stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getDiscountedItems(Long restaurantId) {
        log.info("Fetching discounted items for restaurant ID: {}", restaurantId);

        List<MenuItem> menuItems = menuItemRepository.findDiscountedItems(restaurantId);
        return menuItems.stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getItemsByPriceRange(Long restaurantId, BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Fetching items for restaurant ID: {} within price range {} - {}", restaurantId, minPrice, maxPrice);

        List<MenuItem> menuItems = menuItemRepository.findByPriceRange(restaurantId, minPrice, maxPrice);
        return menuItems.stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MenuItemResponse toggleAvailability(Long restaurantId, Long itemId) {
        log.info("Toggling availability for menu item ID: {} in restaurant ID: {}", itemId, restaurantId);

        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + itemId));

        menuItem.setIsAvailable(!menuItem.getIsAvailable());
        MenuItem updatedItem = menuItemRepository.save(menuItem);

        log.info("Menu item availability toggled to: {}", updatedItem.getIsAvailable());
        return mapToMenuItemResponse(updatedItem);
    }

    // Helper method
    private MenuItemResponse mapToMenuItemResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .discountedPrice(menuItem.getDiscountedPrice())
                .dietaryType(menuItem.getDietaryType())
                .spiceLevel(menuItem.getSpiceLevel())
                .isAvailable(menuItem.getIsAvailable())
                .isBestseller(menuItem.getIsBestseller())
                .imageUrl(menuItem.getImageUrl())
                .preparationTimeMinutes(menuItem.getPreparationTime())
                .categoryId(menuItem.getCategoryId())
                .categoryName(menuItem.getCategory() != null ? menuItem.getCategory().getName() : null)
                .build();
    }
}