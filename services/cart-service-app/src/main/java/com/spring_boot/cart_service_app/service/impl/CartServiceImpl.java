package com.spring_boot.cart_service_app.service.impl;

import com.spring_boot.cart_service_app.client.RestaurantServiceClient;
import com.spring_boot.cart_service_app.client.UserServiceClient;
import com.spring_boot.cart_service_app.dto.request.AddToCartRequest;
import com.spring_boot.cart_service_app.dto.request.UpdateCartItemRequest;
import com.spring_boot.cart_service_app.dto.response.CartItemResponse;
import com.spring_boot.cart_service_app.dto.response.CartResponse;
import com.spring_boot.cart_service_app.entity.Cart;
import com.spring_boot.cart_service_app.entity.CartItem;
import com.spring_boot.cart_service_app.exception.CartNotFoundException;
import com.spring_boot.cart_service_app.exception.InvalidCartOperationException;
import com.spring_boot.cart_service_app.repository.CartItemRepository;
import com.spring_boot.cart_service_app.repository.CartRepository;
import com.spring_boot.cart_service_app.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserServiceClient userServiceClient;
    private final RestaurantServiceClient restaurantServiceClient;

    @Value("${cart.expiry.minutes:30}")
    private Integer cartExpiryMinutes;

    @Value("${cart.max-items:50}")
    private Integer maxCartItems;

    @Override
    @CacheEvict(value = "carts", key = "#userId")
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        log.info("Adding item to cart for user: {}, restaurant: {}, menuItem: {}",
                userId, request.getRestaurantId(), request.getMenuItemId());

        // Verify user exists
        verifyUser(userId);

        // Get or create cart
        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseGet(() -> createNewCart(userId, request.getRestaurantId()));

        // Validate same restaurant
        if (!cart.getRestaurantId().equals(request.getRestaurantId())) {
            throw new InvalidCartOperationException(
                    "Cannot add items from different restaurants. Clear cart first.");
        }

        // Check max items limit
        if (cart.getItems().size() >= maxCartItems) {
            throw new InvalidCartOperationException(
                    "Cart is full. Maximum " + maxCartItems + " items allowed.");
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndMenuItemId(cart.getId(), request.getMenuItemId());

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.calculateSubtotal();
            cartItemRepository.save(item);
            log.info("Updated existing cart item quantity to: {}", item.getQuantity());
        } else {
            // Create new cart item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .menuItemId(request.getMenuItemId())
                    .itemName(request.getItemName())
                    .price(request.getPrice())
                    .quantity(request.getQuantity())
                    .customizations(request.getCustomizations())
                    .imageUrl(request.getImageUrl())
                    .build();
            newItem.calculateSubtotal();

            cart.addItem(newItem);
            cartItemRepository.save(newItem);
            log.info("Added new item to cart: {}", newItem.getItemName());
        }

        // Update cart totals and expiry
        cart.recalculateTotal();
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(cartExpiryMinutes));
        cart = cartRepository.save(cart);

        log.info("Cart updated successfully. Total items: {}, Total amount: {}",
                cart.getTotalItems(), cart.getTotalAmount());

        return mapToCartResponse(cart);
    }

    @Override
    @Cacheable(value = "carts", key = "#userId")
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        log.info("Fetching cart for user: {}", userId);

        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("No active cart found for user"));

        // Check if cart expired
        if (cart.getExpiresAt() != null && cart.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Cart expired for user: {}", userId);
            throw new InvalidCartOperationException("Cart has expired. Please add items again.");
        }

        return mapToCartResponse(cart);
    }

    @Override
    @CacheEvict(value = "carts", key = "#userId")
    public CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        log.info("Updating cart item: {} for user: {}", cartItemId, userId);

        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("No active cart found"));

        CartItem cartItem = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new InvalidCartOperationException("Cart item not found"));

        cartItem.setQuantity(request.getQuantity());
        if (request.getCustomizations() != null) {
            cartItem.setCustomizations(request.getCustomizations());
        }
        cartItem.calculateSubtotal();
        cartItemRepository.save(cartItem);

        cart.recalculateTotal();
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(cartExpiryMinutes));
        cart = cartRepository.save(cart);

        log.info("Cart item updated successfully");
        return mapToCartResponse(cart);
    }

    @Override
    @CacheEvict(value = "carts", key = "#userId")
    public CartResponse removeCartItem(Long userId, Long cartItemId) {
        log.info("Removing cart item: {} for user: {}", cartItemId, userId);

        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("No active cart found"));

        CartItem cartItem = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new InvalidCartOperationException("Cart item not found"));

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        cart.recalculateTotal();
        cart = cartRepository.save(cart);

        log.info("Cart item removed successfully");
        return mapToCartResponse(cart);
    }

    @Override
    @CacheEvict(value = "carts", key = "#userId")
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);

        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("No active cart found"));

        cart.setIsActive(false);
        cartRepository.save(cart);

        log.info("Cart cleared successfully for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse validateCart(Long userId) {
        log.info("Validating cart for user: {}", userId);

        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("No active cart found"));

        if (cart.getItems().isEmpty()) {
            throw new InvalidCartOperationException("Cart is empty");
        }

        if (cart.getExpiresAt() != null && cart.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCartOperationException("Cart has expired");
        }

        // TODO: Validate with Restaurant Service
        // - Check restaurant is active and accepting orders
        // - Check all menu items are available
        // - Verify prices haven't changed

        log.info("Cart validation successful");
        return mapToCartResponse(cart);
    }

    // Helper methods

    private Cart createNewCart(Long userId, Long restaurantId) {
        log.info("Creating new cart for user: {}, restaurant: {}", userId, restaurantId);

        // TODO: Fetch restaurant name from Restaurant Service
        String restaurantName = "Restaurant-" + restaurantId;

        Cart cart = Cart.builder()
                .userId(userId)
                .restaurantId(restaurantId)
                .restaurantName(restaurantName)
                .expiresAt(LocalDateTime.now().plusMinutes(cartExpiryMinutes))
                .isActive(true)
                .build();

        return cartRepository.save(cart);
    }

    private void verifyUser(Long userId) {
        try {
            userServiceClient.checkUserExists(userId);
        } catch (Exception e) {
            log.error("User validation failed for userId: {}", userId, e);
            throw new InvalidCartOperationException("Invalid user");
        }
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .restaurantId(cart.getRestaurantId())
                .restaurantName(cart.getRestaurantName())
                .items(items)
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .expiresAt(cart.getExpiresAt())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItemId())
                .itemName(item.getItemName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .customizations(item.getCustomizations())
                .imageUrl(item.getImageUrl())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}