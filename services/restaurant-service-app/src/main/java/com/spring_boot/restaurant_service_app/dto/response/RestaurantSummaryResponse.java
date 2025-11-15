package com.spring_boot.restaurant_service_app.dto.response;

import com.spring_boot.restaurant_service_app.entity.enums.CuisineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSummaryResponse {
    private Long id;
    private String name;
    private String description;
    private List<CuisineType> cuisineTypes;
    private Boolean isVeg;
    private BigDecimal averageCostForTwo;
    private BigDecimal rating;
    private Integer totalRatings;
    private Boolean acceptingOrders;
    private String imageUrl;
    private String city;
    private Double distanceInKm; // Distance from user's location
}