package com.mountedge.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public class UserDetailDto {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private long totalOrders;
    private BigDecimal totalSpent;
    private List<OrderSummaryDto> recentOrders;

    public UserDetailDto() {}

    public UserDetailDto(Long userId, String name, String email, String role, long totalOrders, BigDecimal totalSpent, List<OrderSummaryDto> recentOrders) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.totalOrders = totalOrders;
        this.totalSpent = totalSpent;
        this.recentOrders = recentOrders;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }

    public List<OrderSummaryDto> getRecentOrders() { return recentOrders; }
    public void setRecentOrders(List<OrderSummaryDto> recentOrders) { this.recentOrders = recentOrders; }
}
