package com.mountedge.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDetailDto {
    private Long orderId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private String paymentMethod;
    private String shippingAddress;
    private String customerName;
    private String customerEmail;
    private Boolean isBulkOrder;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private List<OrderItemDetailDto> items;

    public OrderDetailDto() {}

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public Boolean getIsBulkOrder() { return isBulkOrder; }
    public void setIsBulkOrder(Boolean isBulkOrder) { this.isBulkOrder = isBulkOrder; }
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public List<OrderItemDetailDto> getItems() { return items; }
    public void setItems(List<OrderItemDetailDto> items) { this.items = items; }

    public static class OrderItemDetailDto {
        private String productName;
        private int quantity;
        private BigDecimal price;
        private BigDecimal originalPrice;

        public OrderItemDetailDto() {}
        public OrderItemDetailDto(String productName, int quantity, BigDecimal price, BigDecimal originalPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
            this.originalPrice = originalPrice;
        }

        // Getters and Setters
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getOriginalPrice() { return originalPrice; }
        public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    }
}
