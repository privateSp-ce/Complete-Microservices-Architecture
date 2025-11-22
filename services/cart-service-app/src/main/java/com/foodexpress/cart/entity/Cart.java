package com.foodexpress.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis Hash for Cart
 * Key pattern: cart:{userId}
 */
@RedisHash("cart")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart implements Serializable {

    @Id
    private String id; // This will be the userId

    @Indexed
    private String userId; // Storing as String for consistency, though originally Long

    private String restaurantId; // String (Mongo ID)
    private String restaurantName;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Builder.Default
    private Integer totalItems = 0;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper methods
    public void addItem(CartItem item) {
        // Check if item already exists
        boolean exists = false;
        for (CartItem existingItem : items) {
            if (existingItem.getItemId().equals(item.getItemId())) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                existingItem.setSubtotal(existingItem.getPrice().multiply(BigDecimal.valueOf(existingItem.getQuantity())));
                exists = true;
                break;
            }
        }
        if (!exists) {
            items.add(item);
        }
        recalculateTotal();
    }

    public void removeItem(String itemId) {
        items.removeIf(item -> item.getItemId().equals(itemId));
        recalculateTotal();
    }

    public void updateItemQuantity(String itemId, Integer quantity) {
        for (CartItem item : items) {
            if (item.getItemId().equals(itemId)) {
                item.setQuantity(quantity);
                item.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(quantity)));
                break;
            }
        }
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        this.updatedAt = LocalDateTime.now();
    }
}
