package com.mountedge.ecommerce.controller;

import com.mountedge.ecommerce.config.UserDetailsImpl;
import com.mountedge.ecommerce.dto.ApiResponse;
import com.mountedge.ecommerce.dto.CartDto;
import com.mountedge.ecommerce.mapper.CartMapper;
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
    private final CartMapper cartMapper;

    public CartController(CartService cartService, CartMapper cartMapper) {
        this.cartService = cartService;
        this.cartMapper = cartMapper;
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(cartMapper.toDto(cartService.getCartByUserId(userDetails.getId())));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addItemToCart(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                   @RequestBody Map<String, Object> payload) {
        Long productId = Long.valueOf(payload.get("productId").toString());
        int quantity = Integer.parseInt(payload.get("quantity").toString());
        
        cartService.addItemToCart(userDetails.getId(), productId, quantity);
        return ResponseEntity.ok(new ApiResponse(true, "Item added to cart"));
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse> updateItemQuantity(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                   @RequestBody Map<String, Object> payload) {
        Long productId = Long.valueOf(payload.get("productId").toString());
        int quantity = Integer.parseInt(payload.get("quantity").toString());
        
        // Using existing service methods: remove then add to simulate an update.
        // A proper update method could be added to CartService, but this works using existing functionality.
        cartService.removeItemFromCart(userDetails.getId(), productId);
        if (quantity > 0) {
            cartService.addItemToCart(userDetails.getId(), productId, quantity);
        }
        return ResponseEntity.ok(new ApiResponse(true, "Item updated in cart"));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse> removeItemFromCart(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        @PathVariable Long productId) {
        cartService.removeItemFromCart(userDetails.getId(), productId);
        return ResponseEntity.ok(new ApiResponse(true, "Item removed from cart"));
    }
}
