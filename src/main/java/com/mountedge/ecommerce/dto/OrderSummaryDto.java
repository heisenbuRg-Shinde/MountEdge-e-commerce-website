package com.mountedge.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSummaryDto {
    private Long orderId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private int itemCount;
    private Boolean isBulkOrder;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    public OrderSummaryDto(Long orderId, BigDecimal totalAmount, String status, LocalDateTime createdAt, int itemCount, Boolean isBulkOrder, BigDecimal discountPercentage, BigDecimal discountAmount) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.itemCount = itemCount;
        this.isBulkOrder = isBulkOrder;
        this.discountPercentage = discountPercentage;
        this.discountAmount = discountAmount;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
    public Boolean getIsBulkOrder() { return isBulkOrder; }
    public void setIsBulkOrder(Boolean isBulkOrder) { this.isBulkOrder = isBulkOrder; }
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
}
