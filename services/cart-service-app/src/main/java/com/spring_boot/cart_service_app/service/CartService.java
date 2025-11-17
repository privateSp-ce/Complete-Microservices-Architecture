package com.spring_boot.cart_service_app.service;

import com.spring_boot.cart_service_app.dto.request.AddToCartRequest;
import com.spring_boot.cart_service_app.dto.request.UpdateCartItemRequest;
import com.spring_boot.cart_service_app.dto.response.CartResponse;

public interface CartService {

    /**
     * Add item to cart or update quantity if already exists
     */
    CartResponse addToCart(Long userId, AddToCartRequest request);

    /**
     * Get current active cart for user
     */
    CartResponse getCart(Long userId);

    /**
     * Update cart item quantity
     */
    CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request);

    /**
     * Remove item from cart
     */
    CartResponse removeCartItem(Long userId, Long cartItemId);

    /**
     * Clear entire cart
     */
    void clearCart(Long userId);

    /**
     * Validate cart before checkout
     */
    CartResponse validateCart(Long userId);
}