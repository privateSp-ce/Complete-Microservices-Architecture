package com.spring_boot.restaurant_service_app.dto.request;

import com.spring_boot.restaurant_service_app.entity.enums.CuisineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSearchRequest {

    private String name;

    private String city;

    private CuisineType cuisineType;

    private Boolean isVeg;

    private Boolean acceptingOrders;

    private Double latitude;

    private Double longitude;

    private Double radiusInKm; // For nearby search

    private BigDecimal minRating;

    private BigDecimal maxCostForTwo;

    // Pagination
    private Integer page;

    private Integer size;

    // Sorting
    private String sortBy; // rating, cost, distance

    private String sortDirection; // asc, desc
}