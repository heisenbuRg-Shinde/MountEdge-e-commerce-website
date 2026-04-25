package com.mountedge.ecommerce.service;

import com.mountedge.ecommerce.dto.AnalyticsDto;
import com.mountedge.ecommerce.repository.OrderRepository;
import com.mountedge.ecommerce.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * AnalyticsService - Demonstrates OOP principles:
 *   - Abstraction: Hides complex MySQL aggregation queries behind simple method calls.
 *   - Single Responsibility: Only responsible for analytics data computation.
 *   - Encapsulation: Data is packaged into AnalyticsDto before returning.
 *
 * Uses native MySQL GROUP BY queries on orders and order_items for efficient aggregation.
 */
@Service
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public AnalyticsService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsDto getAnalytics(LocalDate startDate, LocalDate endDate) {
        AnalyticsDto dto = new AnalyticsDto();

        // Build date range for SQL queries
        String start = startDate.atStartOfDay().toString().replace("T", " ");
        String end = endDate.plusDays(1).atStartOfDay().toString().replace("T", " ");

        // ─── Monthly Revenue & Orders (MySQL DATE_FORMAT grouping) ────────────────
        List<Object[]> monthlyData = orderRepository.findMonthlyRevenueAndOrders(start, end);
        List<String> labels = new ArrayList<>();
        List<BigDecimal> revenues = new ArrayList<>();
        List<Long> orders = new ArrayList<>();

        for (Object[] row : monthlyData) {
            labels.add((String) row[0]);                            // e.g. "Apr 2025"
            revenues.add((BigDecimal) row[1]);                     // total revenue
            orders.add(((Number) row[2]).longValue());             // order count
        }
        dto.setMonthLabels(labels);
        dto.setMonthlyRevenue(revenues);
        dto.setMonthlyOrders(orders);

        // ─── Top 5 Best-Selling Products (by quantity sold) ──────────────────────
        List<Object[]> topProducts = orderItemRepository.findTopProductsByQuantity(start, end, 5);
        List<String> productNames = new ArrayList<>();
        List<Long> productSales = new ArrayList<>();
        for (Object[] row : topProducts) {
            productNames.add((String) row[0]);
            productSales.add(((Number) row[1]).longValue());
        }
        dto.setTopProductNames(productNames);
        dto.setTopProductSales(productSales);

        // ─── Category Revenue Breakdown ───────────────────────────────────────────
        List<Object[]> categoryData = orderItemRepository.findRevenueByCategory(start, end);
        List<String> catNames = new ArrayList<>();
        List<BigDecimal> catRevenues = new ArrayList<>();
        for (Object[] row : categoryData) {
            catNames.add((String) row[0]);
            catRevenues.add((BigDecimal) row[1]);
        }
        dto.setCategoryNames(catNames);
        dto.setCategoryRevenue(catRevenues);

        // ─── Summary Totals ───────────────────────────────────────────────────────
        BigDecimal totalRevenue = revenues.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalOrders = orders.stream().mapToLong(Long::longValue).sum();
        long totalProductsSold = productSales.stream().mapToLong(Long::longValue).sum();
        BigDecimal avgOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        dto.setTotalRevenue(totalRevenue);
        dto.setTotalOrders(totalOrders);
        dto.setTotalProductsSold(totalProductsSold);
        dto.setAverageOrderValue(avgOrderValue);

        return dto;
    }
}
