package com.mountedge.ecommerce.dto;

import java.math.BigDecimal;

public class CartItemDto {
    private Long cartItemId;
    private ProductDto product;
    private Integer quantity;
    private BigDecimal itemTotal;

    public CartItemDto() {}

    public CartItemDto(Long cartItemId, ProductDto product, Integer quantity, BigDecimal itemTotal) {
        this.cartItemId = cartItemId;
        this.product = product;
        this.quantity = quantity;
        this.itemTotal = itemTotal;
    }

    public Long getCartItemId() { return cartItemId; }
    public void setCartItemId(Long cartItemId) { this.cartItemId = cartItemId; }
    public ProductDto getProduct() { return product; }
    public void setProduct(ProductDto product) { this.product = product; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getItemTotal() { return itemTotal; }
    public void setItemTotal(BigDecimal itemTotal) { this.itemTotal = itemTotal; }
}
