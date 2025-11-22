package com.foodexpress.restaurant.entity;

import com.foodexpress.restaurant.entity.enums.CuisineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Restaurant entity representing a restaurant in the system
 * Owned by a user (restaurant owner)
 */
@Document(collection = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    private String id;

    /**
     * Foreign key to User Service (user_id)
     * This links to the restaurant owner in the User Service
     */
    @Field("owner_user_id")
    private Long ownerUserId;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    /**
     * Store multiple cuisine types as comma-separated string
     * Example: "INDIAN,CHINESE,CONTINENTAL"
     */
    @Field("cuisine_types")
    private String cuisineTypes;

    @Field("full_address")
    private String fullAddress;

    @Field("city")
    private String city;

    @Field("state")
    private String state;

    @Field("pincode")
    private String pincode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String phone;

    private String email;

    @Field("average_rating")
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Field("total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    @Field("is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Field("is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Field("is_accepting_orders")
    @Builder.Default
    private Boolean isAcceptingOrders = true;

    @Field("opening_time")
    private LocalTime openingTime;

    @Field("closing_time")
    private LocalTime closingTime;

    @Field("average_preparation_time")
    private Integer averagePreparationTime; // in minutes

    @Field("minimum_order_amount")
    private BigDecimal minimumOrderAmount;

    @Field("delivery_fee")
    private BigDecimal deliveryFee;

    @Field("logo_url")
    private String logoUrl;

    @Field("banner_url")
    private String bannerUrl;

    /**
     * One restaurant can have many menu categories
     * In Mongo, we can embed them or reference them.
     * Since menu categories are strictly bound to a restaurant, embedding is a good choice,
     * BUT for scalability if menus get huge, DBRef is safer.
     * Given typical restaurant sizes, DBRef is a good middle ground or keeping them separate collections.
     * Let's use separate collections and DBRef/Manual Reference.
     */
    @DBRef
    @Builder.Default
    private List<MenuCategory> menuCategories = new ArrayList<>();

    /**
     * One restaurant can have many menu items
     */
    @DBRef
    @Builder.Default
    private List<MenuItem> menuItems = new ArrayList<>();

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public void addMenuCategory(MenuCategory category) {
        menuCategories.add(category);
        category.setRestaurantId(this.id);
    }

    public void removeMenuCategory(MenuCategory category) {
        menuCategories.remove(category);
        category.setRestaurantId(null);
    }

    public void addMenuItem(MenuItem item) {
        menuItems.add(item);
        item.setRestaurantId(this.id);
    }

    public void removeMenuItem(MenuItem item) {
        menuItems.remove(item);
        item.setRestaurantId(null);
    }

    /**
     * Check if restaurant is currently open
     */
    public boolean isCurrentlyOpen() {
        if (openingTime == null || closingTime == null) {
            return true; // 24/7 open if no times set
        }
        LocalTime now = LocalTime.now();
        return now.isAfter(openingTime) && now.isBefore(closingTime);
    }

    /**
     * Parse cuisine types from comma-separated string
     */
    public List<CuisineType> getCuisineTypesList() {
        if (cuisineTypes == null || cuisineTypes.isEmpty()) {
            return new ArrayList<>();
        }
        List<CuisineType> types = new ArrayList<>();
        for (String type : cuisineTypes.split(",")) {
            try {
                types.add(CuisineType.valueOf(type.trim()));
            } catch (IllegalArgumentException e) {
                // Skip invalid types
            }
        }
        return types;
    }

    /**
     * Set cuisine types from list
     */
    public void setCuisineTypesList(List<CuisineType> types) {
        if (types == null || types.isEmpty()) {
            this.cuisineTypes = null;
            return;
        }
        this.cuisineTypes = String.join(",",
                types.stream().map(Enum::name).toArray(String[]::new));
    }
}
