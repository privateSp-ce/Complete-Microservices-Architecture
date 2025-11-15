package com.spring_cloud.user_service_app.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spring_cloud.user_service_app.entity.Address;
import com.spring_cloud.user_service_app.entity.enums.AddressLabel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for address information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {

    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    private AddressLabel label;

    @JsonProperty("full_address")
    private String fullAddress;

    private String landmark;

    private String city;

    private String state;

    private String pincode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @JsonProperty("is_default")
    private Boolean isDefault;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    /**
     * Convert Address entity to AddressResponse DTO
     */
    public static AddressResponse fromEntity(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .userId(address.getUser().getId())
                .label(address.getLabel())
                .fullAddress(address.getFullAddress())
                .landmark(address.getLandmark())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}