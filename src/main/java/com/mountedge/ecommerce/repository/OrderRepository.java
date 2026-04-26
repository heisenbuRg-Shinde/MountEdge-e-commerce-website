package com.mountedge.ecommerce.repository;

import com.mountedge.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Native MySQL query: groups orders by month using DATE_FORMAT.
     * Returns [monthLabel, totalRevenue, orderCount] for the given date range.
     * Demonstrates DBMS concept: Aggregation with GROUP BY and date functions.
     */
    @Query(value = """
        SELECT DATE_FORMAT(o.created_at, '%b %Y') AS month,
               SUM(o.total_amount)                AS revenue,
               COUNT(o.order_id)                  AS orderCount
        FROM orders o
        WHERE o.created_at BETWEEN :start AND :end
        GROUP BY DATE_FORMAT(o.created_at, '%Y-%m')
        ORDER BY MIN(o.created_at)
        """, nativeQuery = true)
    List<Object[]> findMonthlyRevenueAndOrders(@Param("start") String start,
                                               @Param("end") String end);
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    java.math.BigDecimal sumTotalAmount();
}
