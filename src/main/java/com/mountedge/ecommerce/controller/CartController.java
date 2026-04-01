package com.mountedge.ecommerce.controller;

import com.mountedge.ecommerce.config.UserDetailsImpl;
import com.mountedge.ecommerce.dto.ApiResponse;
import com.mountedge.ecommerce.entity.Cart;
import com.mountedge.ecommerce.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(cartService.getCartByUserId(userDetails.getId()));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addItemToCart(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                   @RequestBody Map<String, Object> payload) {
        Long productId = Long.valueOf(payload.get("productId").toString());
        int quantity = Integer.parseInt(payload.get("quantity").toString());
        
        cartService.addItemToCart(userDetails.getId(), productId, quantity);
        return ResponseEntity.ok(new ApiResponse(true, "Item added to cart"));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse> removeItemFromCart(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        @PathVariable Long productId) {
        cartService.removeItemFromCart(userDetails.getId(), productId);
        return ResponseEntity.ok(new ApiResponse(true, "Item removed from cart"));
    }
}
