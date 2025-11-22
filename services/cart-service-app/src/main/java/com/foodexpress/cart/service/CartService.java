package com.foodexpress.cart.service;

import com.foodexpress.cart.dto.request.AddToCartRequest;
import com.foodexpress.cart.dto.request.UpdateCartItemRequest;
import com.foodexpress.cart.dto.response.CartResponse;

public interface CartService {

    /**
     * Add item to cart or update quantity if already exists
     */
    CartResponse addToCart(String userId, AddToCartRequest request);

    /**
     * Get current active cart for user
     */
    CartResponse getCart(String userId);

    /**
     * Update cart item quantity
     */
    CartResponse updateCartItem(String userId, String itemId, UpdateCartItemRequest request);

    /**
     * Remove item from cart
     */
    CartResponse removeCartItem(String userId, String itemId);

    /**
     * Clear entire cart
     */
    void clearCart(String userId);

}
