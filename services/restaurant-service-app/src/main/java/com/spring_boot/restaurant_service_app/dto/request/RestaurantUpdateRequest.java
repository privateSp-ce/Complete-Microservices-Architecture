package com.spring_boot.restaurant_service_app.dto.request;

import com.spring_boot.restaurant_service_app.dto.common.AddressDTO;
import com.spring_boot.restaurant_service_app.dto.common.GeoLocationDTO;
import com.spring_boot.restaurant_service_app.entity.enums.CuisineType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class RestaurantUpdateRequest {

    @Size(min = 3, max = 255, message = "Restaurant name must be between 3 and 255 characters")
    private String name;

    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    private AddressDTO address;

    private GeoLocationDTO location;

    private List<CuisineType> cuisineTypes;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String email;

    private LocalTime openingTime;

    private LocalTime closingTime;

    @DecimalMin(value = "0.0", inclusive = false, message = "Average cost must be greater than 0")
    private BigDecimal averageCostForTwo;

    private BigDecimal minimumOrderAmount;

    private BigDecimal deliveryFee;

    private String imageUrl;

    private Boolean isAcceptingOrders;

    private Boolean isActive;
}