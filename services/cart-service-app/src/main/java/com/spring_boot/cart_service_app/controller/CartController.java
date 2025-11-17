package com.spring_boot.cart_service_app.controller;

import com.spring_boot.cart_service_app.dto.request.AddToCartRequest;
import com.spring_boot.cart_service_app.dto.request.UpdateCartItemRequest;
import com.spring_boot.cart_service_app.dto.common.ApiResponse;
import com.spring_boot.cart_service_app.dto.response.CartResponse;
import com.spring_boot.cart_service_app.service.CartService;
import io.micrometer.tracing.Tracer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@Slf4j
public class CartController {

    private final CartService cartService;
    private final Tracer tracer;

    public CartController(CartService cartService,
                          @Autowired(required = false) Tracer tracer) {
        this.cartService = cartService;
        this.tracer = tracer;
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "no-trace-id";
    }

    /**
     * Add item to cart
     * POST /api/v1/cart/items
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddToCartRequest request) {

        String traceId = getTraceId();
        log.info("Add to cart request for user: {}, TraceId: {}", userId, traceId);

        CartResponse cartResponse = cartService.addToCart(userId, request);

        ApiResponse<CartResponse> response = ApiResponse.success(
                cartResponse,
                "Item added to cart successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get current cart
     * GET /api/v1/cart
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestHeader("X-User-Id") Long userId) {

        String traceId = getTraceId();
        log.info("Get cart request for user: {}, TraceId: {}", userId, traceId);

        CartResponse cartResponse = cartService.getCart(userId);

        ApiResponse<CartResponse> response = ApiResponse.success(
                cartResponse,
                "Cart fetched successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Update cart item
     * PUT /api/v1/cart/items/{itemId}
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        String traceId = getTraceId();
        log.info("Update cart item request: {}, user: {}, TraceId: {}", itemId, userId, traceId);

        CartResponse cartResponse = cartService.updateCartItem(userId, itemId, request);

        ApiResponse<CartResponse> response = ApiResponse.success(
                cartResponse,
                "Cart item updated successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Remove item from cart
     * DELETE /api/v1/cart/items/{itemId}
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId) {

        String traceId = getTraceId();
        log.info("Remove cart item request: {}, user: {}, TraceId: {}", itemId, userId, traceId);

        CartResponse cartResponse = cartService.removeCartItem(userId, itemId);

        ApiResponse<CartResponse> response = ApiResponse.success(
                cartResponse,
                "Item removed from cart successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Clear cart
     * DELETE /api/v1/cart
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader("X-User-Id") Long userId) {

        String traceId = getTraceId();
        log.info("Clear cart request for user: {}, TraceId: {}", userId, traceId);

        cartService.clearCart(userId);

        ApiResponse<Void> response = ApiResponse.success(
                "Cart cleared successfully",
                traceId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Validate cart before checkout
     * POST /api/v1/cart/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<CartResponse>> validateCart(
            @RequestHeader("X-User-Id") Long userId) {

        String traceId = getTraceId();
        log.info("Validate cart request for user: {}, TraceId: {}", userId, traceId);

        CartResponse cartResponse = cartService.validateCart(userId);

        ApiResponse<CartResponse> response = ApiResponse.success(
                cartResponse,
                "Cart is valid",
                traceId
        );

        return ResponseEntity.ok(response);
    }
}