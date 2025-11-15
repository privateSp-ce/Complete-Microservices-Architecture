package com.spring_cloud.user_service_app.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spring_cloud.user_service_app.entity.User;
import com.spring_cloud.user_service_app.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for user information
 * IMPORTANT: Never include password field!
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;

    private String email;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String phone;

    private UserRole role;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    @JsonProperty("phone_verified")
    private Boolean phoneVerified;

    private List<AddressResponse> addresses;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    /**
     * Convert User entity to UserResponse DTO
     */
    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Convert User entity to UserResponse DTO with addresses
     */
    public static UserResponse fromEntityWithAddresses(User user) {
        UserResponse response = fromEntity(user);
        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
            response.setAddresses(
                    user.getAddresses().stream()
                            .map(AddressResponse::fromEntity)
                            .collect(Collectors.toList())
            );
        }
        return response;
    }
}