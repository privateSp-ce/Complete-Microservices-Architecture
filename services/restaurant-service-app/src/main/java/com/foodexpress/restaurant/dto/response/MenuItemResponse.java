package com.foodexpress.restaurant.dto.response;

import com.foodexpress.restaurant.entity.enums.DietaryType;
import com.foodexpress.restaurant.entity.enums.SpiceLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private DietaryType dietaryType;
    private SpiceLevel spiceLevel;
    private Boolean isAvailable;
    private Boolean isBestseller;
    private String imageUrl;
    private Integer preparationTimeMinutes;
    private Long categoryId;
    private String categoryName;
}