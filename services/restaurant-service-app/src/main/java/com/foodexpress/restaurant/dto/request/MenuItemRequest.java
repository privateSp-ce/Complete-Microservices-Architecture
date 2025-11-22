package com.foodexpress.restaurant.dto.request;

import com.foodexpress.restaurant.entity.enums.DietaryType;
import com.foodexpress.restaurant.entity.enums.SpiceLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemRequest {

    @NotBlank(message = "Item name is required")
    @Size(min = 2, max = 255, message = "Item name must be between 2 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, message = "Discounted price must be greater than 0")
    private BigDecimal discountedPrice;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Dietary type is required")
    private DietaryType dietaryType;

    private SpiceLevel spiceLevel;

    private Boolean isAvailable;

    private Boolean isBestseller;

    private String imageUrl;

    @Min(value = 1, message = "Preparation time must be at least 1 minute")
    private Integer preparationTime;

    @Min(value = 0, message = "Calories cannot be negative")
    private Integer calories;

    @Min(value = 1, message = "Serves count must be at least 1")
    private Integer servesCount;

    private Boolean isVegan;

    private Boolean isGlutenFree;
}