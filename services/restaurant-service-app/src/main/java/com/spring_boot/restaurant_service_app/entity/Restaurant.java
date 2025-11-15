package com.spring_boot.restaurant_service_app.entity;

import com.spring_boot.restaurant_service_app.entity.enums.CuisineType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Restaurant entity representing a restaurant in the system
 * Owned by a user (restaurant owner)
 */
@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key to User Service (user_id)
     * This links to the restaurant owner in the User Service
     */
    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Store multiple cuisine types as comma-separated string
     * Example: "INDIAN,CHINESE,CONTINENTAL"
     */
    @Column(name = "cuisine_types", length = 500)
    private String cuisineTypes;

    @Column(name = "full_address", nullable = false, columnDefinition = "TEXT")
    private String fullAddress;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 10)
    private String pincode;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(length = 15)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(name = "average_rating", precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "is_accepting_orders", nullable = false)
    @Builder.Default
    private Boolean isAcceptingOrders = true;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "average_preparation_time")
    private Integer averagePreparationTime; // in minutes

    @Column(name = "minimum_order_amount", precision = 10, scale = 2)
    private BigDecimal minimumOrderAmount;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    /**
     * One restaurant can have many menu categories
     */
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuCategory> menuCategories = new ArrayList<>();

    /**
     * One restaurant can have many menu items
     */
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuItem> menuItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public void addMenuCategory(MenuCategory category) {
        menuCategories.add(category);
        category.setRestaurant(this);
    }

    public void removeMenuCategory(MenuCategory category) {
        menuCategories.remove(category);
        category.setRestaurant(null);
    }

    public void addMenuItem(MenuItem item) {
        menuItems.add(item);
        item.setRestaurant(this);
    }

    public void removeMenuItem(MenuItem item) {
        menuItems.remove(item);
        item.setRestaurant(null);
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