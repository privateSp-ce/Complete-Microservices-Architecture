package com.foodexpress.restaurant.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu Category entity
 * Examples: Starters, Main Course, Desserts, Beverages
 */
@Document(collection = "menu_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuCategory {

    @Id
    private String id;

    // References parent restaurant
    @Field("restaurant_id")
    @Indexed
    private String restaurantId;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Field("is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * One category can have many menu items
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
}
