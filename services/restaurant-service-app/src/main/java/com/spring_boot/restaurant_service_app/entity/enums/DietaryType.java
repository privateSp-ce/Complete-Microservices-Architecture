package com.spring_boot.restaurant_service_app.entity.enums;

/**
 * Enum for dietary classifications
 */
public enum DietaryType {
    VEGETARIAN,     // No meat, fish, or poultry
    NON_VEGETARIAN, // Contains meat, fish, or poultry
    VEGAN,          // No animal products
    EGGETARIAN,     // Vegetarian + eggs
    JAIN            // No onion, garlic, root vegetables
}