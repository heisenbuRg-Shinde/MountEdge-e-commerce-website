package com.mountedge.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Admin Analytics data.
 * Follows OOP principle of encapsulation - groups all analytics data into one object.
 * Used to feed Chart.js charts on the admin frontend.
 */
public class AnalyticsDto {

    // Monthly revenue/orders chart data
    private List<String> monthLabels;
    private List<BigDecimal> monthlyRevenue;
    private List<Long> monthlyOrders;

    // Top products bar chart
    private List<String> topProductNames;
    private List<Long> topProductSales;

    // Category doughnut chart
    private List<String> categoryNames;
    private List<BigDecimal> categoryRevenue;

    // Summary stats
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long totalProductsSold;
    private BigDecimal averageOrderValue;
    
    // For Excel Analysis Report
    private List<Object[]> productSalesAnalysis;
    private List<Object[]> orderAuditTrail;

    public AnalyticsDto() {}

    // --- Getters & Setters ---

    public List<String> getMonthLabels() { return monthLabels; }
    public void setMonthLabels(List<String> monthLabels) { this.monthLabels = monthLabels; }

    public List<BigDecimal> getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(List<BigDecimal> monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }

    public List<Long> getMonthlyOrders() { return monthlyOrders; }
    public void setMonthlyOrders(List<Long> monthlyOrders) { this.monthlyOrders = monthlyOrders; }

    public List<String> getTopProductNames() { return topProductNames; }
    public void setTopProductNames(List<String> topProductNames) { this.topProductNames = topProductNames; }

    public List<Long> getTopProductSales() { return topProductSales; }
    public void setTopProductSales(List<Long> topProductSales) { this.topProductSales = topProductSales; }

    public List<String> getCategoryNames() { return categoryNames; }
    public void setCategoryNames(List<String> categoryNames) { this.categoryNames = categoryNames; }

    public List<BigDecimal> getCategoryRevenue() { return categoryRevenue; }
    public void setCategoryRevenue(List<BigDecimal> categoryRevenue) { this.categoryRevenue = categoryRevenue; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public long getTotalProductsSold() { return totalProductsSold; }
    public void setTotalProductsSold(long totalProductsSold) { this.totalProductsSold = totalProductsSold; }

    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }

    public List<Object[]> getProductSalesAnalysis() { return productSalesAnalysis; }
    public void setProductSalesAnalysis(List<Object[]> productSalesAnalysis) { this.productSalesAnalysis = productSalesAnalysis; }

    public List<Object[]> getOrderAuditTrail() { return orderAuditTrail; }
    public void setOrderAuditTrail(List<Object[]> orderAuditTrail) { this.orderAuditTrail = orderAuditTrail; }
}
