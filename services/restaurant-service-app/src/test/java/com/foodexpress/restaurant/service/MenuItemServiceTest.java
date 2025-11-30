package com.foodexpress.restaurant.service;

import com.foodexpress.restaurant.dto.request.MenuItemRequest;
import com.foodexpress.restaurant.dto.response.MenuItemResponse;
import com.foodexpress.restaurant.entity.MenuCategory;
import com.foodexpress.restaurant.entity.MenuItem;
import com.foodexpress.restaurant.entity.Restaurant;
import com.foodexpress.restaurant.entity.enums.DietaryType;
import com.foodexpress.restaurant.repository.MenuCategoryRepository;
import com.foodexpress.restaurant.repository.MenuItemRepository;
import com.foodexpress.restaurant.repository.RestaurantRepository;
import com.foodexpress.restaurant.service.impl.MenuItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuCategoryRepository menuCategoryRepository;

    @InjectMocks
    private MenuItemServiceImpl menuItemService;

    @Test
    void createMenuItem_ShouldSaveAndReturnResponse() {
        // Arrange
        String restaurantId = "rest-1";
        String categoryId = "cat-1";

        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        MenuCategory category = new MenuCategory();
        category.setId(categoryId);
        category.setName("Starters");

        MenuItemRequest request = MenuItemRequest.builder()
                .name("Pizza")
                .description("Cheese Pizza")
                .price(BigDecimal.valueOf(10.0))
                .categoryId(categoryId)
                .dietaryType(DietaryType.VEGETARIAN)
                .build();

        MenuItem savedItem = MenuItem.builder()
                .id("item-1")
                .name("Pizza")
                .price(BigDecimal.valueOf(10.0))
                .categoryId(categoryId)
                .dietaryType(DietaryType.VEGETARIAN)
                .build();

        when(restaurantRepository.findByIdAndIsActiveTrue(restaurantId)).thenReturn(Optional.of(restaurant));
        when(menuCategoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)).thenReturn(Optional.of(category));
        when(menuCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(menuItemRepository.existsByNameAndRestaurantId(request.getName(), restaurantId)).thenReturn(false);
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(savedItem);

        // Act
        MenuItemResponse response = menuItemService.createMenuItem(restaurantId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Pizza", response.getName());
        assertEquals("Starters", response.getCategoryName());
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    @Test
    void searchMenuItems_ShouldReturnMatches() {
        // Arrange
        String restaurantId = "rest-1";
        String query = "Pizza";

        MenuItem item = new MenuItem();
        item.setName("Pepperoni Pizza");
        item.setRestaurantId(restaurantId);

        when(menuItemRepository.searchByName(restaurantId, query)).thenReturn(List.of(item));

        // Act
        List<MenuItemResponse> results = menuItemService.searchMenuItems(restaurantId, query);

        // Assert
        assertEquals(1, results.size());
        assertEquals("Pepperoni Pizza", results.get(0).getName());
    }
}
