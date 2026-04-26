package com.mountedge.ecommerce.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduledOrderDto {
    private Long id;
    private String userName;
    private String userEmail;
    private String status;
    private Integer dayOfMonth;
    private LocalDate nextRunDate;
    private String paymentMethod;
    private String addressSummary;
    private String notes;
    private LocalDateTime createdAt;
    private List<ScheduledOrderItemDto> items;

    public ScheduledOrderDto() {}

    public ScheduledOrderDto(Long id, String userName, String userEmail, String status, Integer dayOfMonth, LocalDate nextRunDate, String paymentMethod, String addressSummary, String notes, LocalDateTime createdAt, List<ScheduledOrderItemDto> items) {
        this.id = id;
        this.userName = userName;
        this.userEmail = userEmail;
        this.status = status;
        this.dayOfMonth = dayOfMonth;
        this.nextRunDate = nextRunDate;
        this.paymentMethod = paymentMethod;
        this.addressSummary = addressSummary;
        this.notes = notes;
        this.createdAt = createdAt;
        this.items = items;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(Integer dayOfMonth) { this.dayOfMonth = dayOfMonth; }
    public LocalDate getNextRunDate() { return nextRunDate; }
    public void setNextRunDate(LocalDate nextRunDate) { this.nextRunDate = nextRunDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getAddressSummary() { return addressSummary; }
    public void setAddressSummary(String addressSummary) { this.addressSummary = addressSummary; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<ScheduledOrderItemDto> getItems() { return items; }
    public void setItems(List<ScheduledOrderItemDto> items) { this.items = items; }

    public static class ScheduledOrderItemDto {
        private Long productId;
        private String productName;
        private Integer quantity;

        public ScheduledOrderItemDto() {}

        public ScheduledOrderItemDto(Long productId, String productName, Integer quantity) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
