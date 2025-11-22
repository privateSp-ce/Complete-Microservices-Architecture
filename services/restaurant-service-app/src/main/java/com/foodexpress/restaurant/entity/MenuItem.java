package com.foodexpress.restaurant.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.foodexpress.restaurant.entity.enums.DietaryType;
import com.foodexpress.restaurant.entity.enums.SpiceLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Menu Item entity representing a food item in a restaurant's menu
 */
@Document(collection = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    private String id;

    // Add this new field
    @Field("restaurant_id")
    @Indexed
    private String restaurantId;

    // Same for category
    @Field("category_id")
    @Indexed
    private String categoryId;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("price")
    private BigDecimal price;

    @Field("discounted_price")
    private BigDecimal discountedPrice;

    @Field("image_url")
    private String imageUrl;

    @Field("dietary_type")
    @Builder.Default
    private DietaryType dietaryType = DietaryType.VEGETARIAN;

    @Field("is_vegan")
    @Builder.Default
    private Boolean isVegan = false;

    @Field("is_gluten_free")
    @Builder.Default
    private Boolean isGlutenFree = false;

    @Field("spice_level")
    private SpiceLevel spiceLevel;

    @Field("is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @Field("is_bestseller")
    @Builder.Default
    private Boolean isBestseller = false;

    @Field("preparation_time")
    private Integer preparationTime; // in minutes

    private Integer calories;

    @Field("serves_count")
    @Builder.Default
    private Integer servesCount = 1; // Number of people it serves

    @Field("average_rating")
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Field("total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    /**
     * Get effective price (discounted if available, otherwise regular price)
     */
    public BigDecimal getEffectivePrice() {
        return discountedPrice != null ? discountedPrice : price;
    }

    /**
     * Check if item is currently on discount
     */
    public boolean isOnDiscount() {
        return discountedPrice != null && discountedPrice.compareTo(price) < 0;
    }

    /**
     * Calculate discount percentage
     */
    public Integer getDiscountPercentage() {
        if (!isOnDiscount()) {
            return 0;
        }
        BigDecimal discount = price.subtract(discountedPrice);
        return discount.multiply(BigDecimal.valueOf(100))
                .divide(price, 0, BigDecimal.ROUND_HALF_UP)
                .intValue();
    }
}
