package com.foodexpress.cart.service.impl;

import com.foodexpress.cart.client.RestaurantServiceClient;
import com.foodexpress.cart.client.UserServiceClient;
import com.foodexpress.cart.dto.request.AddToCartRequest;
import com.foodexpress.cart.dto.request.UpdateCartItemRequest;
import com.foodexpress.cart.dto.response.CartItemResponse;
import com.foodexpress.cart.dto.response.CartResponse;
import com.foodexpress.cart.entity.Cart;
import com.foodexpress.cart.entity.CartItem;
import com.foodexpress.cart.exception.CartNotFoundException;
import com.foodexpress.cart.exception.InvalidCartOperationException;
import com.foodexpress.cart.repository.CartRepository;
import com.foodexpress.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    // private final UserServiceClient userServiceClient; // Can be uncommented when User Service is ready
    // private final RestaurantServiceClient restaurantServiceClient; // Can be uncommented when Restaurant Service is ready

    @Value("${cart.expiry.minutes:30}")
    private Integer cartExpiryMinutes;

    @Value("${cart.max-items:50}")
    private Integer maxCartItems;

    @Override
    public CartResponse addToCart(String userId, AddToCartRequest request) {
        log.info("Adding item to cart for user: {}, restaurant: {}, menuItem: {}",
                userId, request.getRestaurantId(), request.getMenuItemId());

        // Get or create cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId, request.getRestaurantId()));

        // Validate same restaurant
        if (cart.getRestaurantId() != null && !cart.getRestaurantId().equals(request.getRestaurantId())) {
            // If cart is empty/expired but old restaurant ID persists, we can clear it
            if (cart.getItems().isEmpty()) {
                cart.setRestaurantId(request.getRestaurantId());
            } else {
                throw new InvalidCartOperationException(
                        "Cannot add items from different restaurants. Clear cart first.");
            }
        }

        // Check max items limit
        if (cart.getItems().size() >= maxCartItems) {
            throw new InvalidCartOperationException(
                    "Cart is full. Maximum " + maxCartItems + " items allowed.");
        }

        // Create new cart item
        // Note: AddToCartRequest fields need to handle Strings now if they were Longs,
        // but for now I assume Request DTOs are updated or compatible.
        CartItem newItem = CartItem.builder()
                .itemId(String.valueOf(request.getMenuItemId())) // Ensure String
                .itemName(request.getItemName())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .customizations(request.getCustomizations())
                .imageUrl(request.getImageUrl())
                .subtotal(request.getPrice().multiply(java.math.BigDecimal.valueOf(request.getQuantity())))
                .build();

        cart.addItem(newItem);

        // Update cart totals and expiry
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(cartExpiryMinutes)); // requires adding expiresAt to Redis entity or using Redis TTL
        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);

        log.info("Cart updated successfully. Total items: {}, Total amount: {}",
                savedCart.getTotalItems(), savedCart.getTotalAmount());

        return mapToCartResponse(savedCart);
    }

    @Override
    public CartResponse getCart(String userId) {
        log.info("Fetching cart for user: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("No active cart found for user"));

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse updateCartItem(String userId, String itemId, UpdateCartItemRequest request) {
        log.info("Updating cart item: {} for user: {}", itemId, userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("No active cart found"));

        cart.updateItemQuantity(itemId, request.getQuantity());
        Cart savedCart = cartRepository.save(cart);

        log.info("Cart item updated successfully");
        return mapToCartResponse(savedCart);
    }

    @Override
    public CartResponse removeCartItem(String userId, String itemId) {
        log.info("Removing cart item: {} for user: {}", itemId, userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("No active cart found"));

        cart.removeItem(itemId);

        // If empty, we can either delete the cart or keep it empty
        if (cart.getItems().isEmpty()) {
             cart.setRestaurantId(null); // Reset restaurant
        }

        Cart savedCart = cartRepository.save(cart);

        log.info("Cart item removed successfully");
        return mapToCartResponse(savedCart);
    }

    @Override
    public void clearCart(String userId) {
        log.info("Clearing cart for user: {}", userId);

        // In Redis, we can just delete the key
        cartRepository.deleteById(userId);

        log.info("Cart cleared successfully for user: {}", userId);
    }

    // Helper methods

    private Cart createNewCart(String userId, String restaurantId) {
        log.info("Creating new cart for user: {}, restaurant: {}", userId, restaurantId);

        // TODO: Fetch restaurant name from Restaurant Service
        String restaurantName = "Restaurant-" + restaurantId;

        return Cart.builder()
                .id(userId) // Using userId as the ID for the Redis Hash
                .userId(userId)
                .restaurantId(restaurantId)
                .restaurantName(restaurantName)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .id(Long.valueOf(cart.getId().hashCode())) // Mocking a Long ID for response compatibility if needed
                .userId(Long.valueOf(cart.getUserId())) // Assuming user IDs are still numeric
                .restaurantId(cart.getRestaurantId() != null ? Long.valueOf(cart.getRestaurantId().hashCode()) : null) // Mocking Long for compatibility
                .restaurantName(cart.getRestaurantName())
                .items(items)
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                // .expiresAt(cart.getExpiresAt())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(Long.valueOf(item.getItemId().hashCode())) // Mocking Long ID
                .menuItemId(Long.valueOf(item.getItemId().hashCode())) // Mocking
                .itemName(item.getItemName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .customizations(item.getCustomizations())
                .imageUrl(item.getImageUrl())
                .build();
    }
}
