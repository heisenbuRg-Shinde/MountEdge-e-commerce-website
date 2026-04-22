package com.mountedge.ecommerce.dto;

import java.util.List;

public class WishlistDto {
    private Long wishlistId;
    private List<ProductDto> items;

    public WishlistDto(Long wishlistId, List<ProductDto> items) {
        this.wishlistId = wishlistId;
        this.items = items;
    }

    public Long getWishlistId() { return wishlistId; }
    public void setWishlistId(Long wishlistId) { this.wishlistId = wishlistId; }
    public List<ProductDto> getItems() { return items; }
    public void setItems(List<ProductDto> items) { this.items = items; }
}
