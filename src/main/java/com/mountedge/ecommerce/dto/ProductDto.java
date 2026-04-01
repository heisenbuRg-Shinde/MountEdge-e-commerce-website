package com.mountedge.ecommerce.dto;

import java.math.BigDecimal;

public class ProductDto {
    private Long productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String categoryName;
    private Integer stockQuantity;

    public ProductDto() {}

    public ProductDto(Long productId, String name, String description, BigDecimal price, String categoryName, Integer stockQuantity) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryName = categoryName;
        this.stockQuantity = stockQuantity;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}
