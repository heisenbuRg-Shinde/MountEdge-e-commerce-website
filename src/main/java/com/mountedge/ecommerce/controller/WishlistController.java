package com.mountedge.ecommerce.controller;

import com.mountedge.ecommerce.dto.WishlistDto;
import com.mountedge.ecommerce.config.UserDetailsImpl;
import com.mountedge.ecommerce.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WishlistDto> getWishlist(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(wishlistService.getUserWishlist(userDetails.getEmail()));
    }

    @PostMapping("/add/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addProductToWishlist(
            Authentication authentication,
            @PathVariable Long productId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        wishlistService.addProductToWishlist(userDetails.getEmail(), productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> removeProductFromWishlist(
            Authentication authentication,
            @PathVariable Long productId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        wishlistService.removeProductFromWishlist(userDetails.getEmail(), productId);
        return ResponseEntity.ok().build();
    }
}
