package com.foodexpress.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem implements Serializable {

    private String menuItemId; // Using MenuItem ID (String from Mongo)
    private String itemName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private String imageUrl;
    private String customizations; // JSON string or comma separated

    // No @Id here as it is embedded in Cart RedisHash
}
