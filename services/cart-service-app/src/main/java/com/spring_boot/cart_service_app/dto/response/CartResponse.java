package com.spring_boot.cart_service_app.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("restaurant_id")
    private Long restaurantId;

    @JsonProperty("restaurant_name")
    private String restaurantName;

    private List<CartItemResponse> items;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("total_items")
    private Integer totalItems;

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}