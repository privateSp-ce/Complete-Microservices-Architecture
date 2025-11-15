package com.spring_cloud.user_service_app.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after successful login
 * Contains JWT token and user information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    /**
     * JWT access token
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * Token type (always "Bearer")
     */
    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Token expiration time in milliseconds
     */
    @JsonProperty("expires_in")
    private Long expiresIn;

    /**
     * User information
     */
    private UserResponse user;
}