package com.mountedge.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartDto {
    private Long cartId;
    private List<CartItemDto> items;
    private BigDecimal cartTotal;

    public CartDto() {}

    public CartDto(Long cartId, List<CartItemDto> items, BigDecimal cartTotal) {
        this.cartId = cartId;
        this.items = items;
        this.cartTotal = cartTotal;
    }

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }
    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }
    public BigDecimal getCartTotal() { return cartTotal; }
    public void setCartTotal(BigDecimal cartTotal) { this.cartTotal = cartTotal; }
}
