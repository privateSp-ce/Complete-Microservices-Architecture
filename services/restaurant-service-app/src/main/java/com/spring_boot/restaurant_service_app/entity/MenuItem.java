package com.spring_boot.restaurant_service_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spring_boot.restaurant_service_app.entity.enums.DietaryType;
import com.spring_boot.restaurant_service_app.entity.enums.SpiceLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Menu Item entity representing a food item in a restaurant's menu
 */
@Entity
@Table(name = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Add this new field
    @Column(name = "restaurant_id", nullable = false, insertable = false, updatable = false)
    private Long restaurantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnore
    private Restaurant restaurant;

    // Same for category
    @Column(name = "category_id", insertable = false, updatable = false)
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private MenuCategory category;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discounted_price", precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_type", length = 20)
    @Builder.Default
    private DietaryType dietaryType = DietaryType.VEGETARIAN;

    @Column(name = "is_vegan")
    @Builder.Default
    private Boolean isVegan = false;

    @Column(name = "is_gluten_free")
    @Builder.Default
    private Boolean isGlutenFree = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "spice_level", length = 20)
    private SpiceLevel spiceLevel;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "is_bestseller")
    @Builder.Default
    private Boolean isBestseller = false;

    @Column(name = "preparation_time")
    private Integer preparationTime; // in minutes

    @Column
    private Integer calories;

    @Column(name = "serves_count")
    @Builder.Default
    private Integer servesCount = 1; // Number of people it serves

    @Column(name = "average_rating", precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
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