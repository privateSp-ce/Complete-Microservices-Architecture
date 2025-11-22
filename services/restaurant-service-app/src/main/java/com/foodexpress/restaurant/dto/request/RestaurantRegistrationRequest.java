package com.foodexpress.restaurant.dto.request;

import com.foodexpress.restaurant.dto.common.AddressDTO;
import com.foodexpress.restaurant.dto.common.GeoLocationDTO;
import com.foodexpress.restaurant.entity.enums.CuisineType;
import jakarta.validation.constraints.*;
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
public class RestaurantRegistrationRequest {

    @NotBlank(message = "Restaurant name is required")
    @Size(min = 3, max = 255, message = "Restaurant name must be between 3 and 255 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @NotNull(message = "Owner user ID is required")
    private Long ownerUserId;

    @NotNull(message = "Address is required")
    private AddressDTO address;

    @NotNull(message = "Location is required")
    private GeoLocationDTO location;

    @NotEmpty(message = "At least one cuisine type is required")
    private List<CuisineType> cuisineTypes;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Opening time is required")
    private LocalTime openingTime;

    @NotNull(message = "Closing time is required")
    private LocalTime closingTime;

    @NotNull(message = "Average cost for two is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Average cost must be greater than 0")
    private BigDecimal averageCostForTwo;

    private BigDecimal minimumOrderAmount;

    private BigDecimal deliveryFee;

    private String imageUrl;

    private String licenseNumber;
}