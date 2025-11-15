package com.spring_boot.restaurant_service_app.dto.response;

import com.spring_boot.restaurant_service_app.dto.common.AddressDTO;
import com.spring_boot.restaurant_service_app.dto.common.GeoLocationDTO;
import com.spring_boot.restaurant_service_app.entity.enums.CuisineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String name;
    private String description;
    private String ownerUserId;
    private String ownerName;
    private String phoneNumber;
    private String email;

    private AddressDTO address;
    private GeoLocationDTO location;

    private List<CuisineType> cuisineTypes;

    private Boolean isVeg;
    private Boolean hasNonVeg;
    private Boolean hasVegan;
    private Boolean hasJainFood;

    private LocalTime openingTime;
    private LocalTime closingTime;

    private BigDecimal averageCostForTwo;
    private BigDecimal rating;
    private Integer totalRatings;

    private Boolean isActive;
    private Boolean isVerified;
    private Boolean acceptingOrders;

    private String imageUrl;
    private String licenseNumber;
}