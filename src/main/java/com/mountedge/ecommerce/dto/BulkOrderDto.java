package com.mountedge.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BulkOrderDto {
    private Long orderId;
    private String userName;
    private String userEmail;
    private int totalItems;
    private BigDecimal discountAmount;
    private BigDecimal originalTotal;
    private BigDecimal finalTotal;
    private String status;
    private LocalDateTime createdAt;

    public BulkOrderDto() {}

    public BulkOrderDto(Long orderId, String userName, String userEmail, int totalItems, BigDecimal discountAmount, BigDecimal originalTotal, BigDecimal finalTotal, String status, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.totalItems = totalItems;
        this.discountAmount = discountAmount;
        this.originalTotal = originalTotal;
        this.finalTotal = finalTotal;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getOriginalTotal() { return originalTotal; }
    public void setOriginalTotal(BigDecimal originalTotal) { this.originalTotal = originalTotal; }

    public BigDecimal getFinalTotal() { return finalTotal; }
    public void setFinalTotal(BigDecimal finalTotal) { this.finalTotal = finalTotal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
