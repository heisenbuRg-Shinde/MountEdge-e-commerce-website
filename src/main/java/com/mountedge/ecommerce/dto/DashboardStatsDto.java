package com.mountedge.ecommerce.dto;

import java.math.BigDecimal;

public class DashboardStatsDto {
    private long totalUsers;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long lowStockProducts;

    public DashboardStatsDto(long totalUsers, long totalOrders, BigDecimal totalRevenue, long lowStockProducts) {
        this.totalUsers = totalUsers;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.lowStockProducts = lowStockProducts;
    }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public long getLowStockProducts() { return lowStockProducts; }
    public void setLowStockProducts(long lowStockProducts) { this.lowStockProducts = lowStockProducts; }
}
