package com.mountedge.ecommerce.repository;

import com.mountedge.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Native MySQL query: JOINs order_items → products → orders.
     * Groups by product to SUM quantities and finds the top N bestsellers.
     * Demonstrates DBMS: Multi-table JOIN + GROUP BY + ORDER BY + LIMIT.
     */
    @Query(value = """
        SELECT p.name          AS productName,
               SUM(oi.quantity) AS totalSold
        FROM order_items oi
        JOIN products p  ON oi.product_id = p.product_id
        JOIN orders o    ON oi.order_id   = o.order_id
        WHERE o.created_at BETWEEN :start AND :end
        GROUP BY p.product_id, p.name
        ORDER BY totalSold DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopProductsByQuantity(@Param("start") String start,
                                             @Param("end") String end,
                                             @Param("limit") int limit);

    /**
     * Native MySQL query: JOINs order_items → products → categories → orders.
     * Computes total revenue per product category.
     * Demonstrates DBMS: Multi-table JOIN + GROUP BY aggregation.
     */
    @Query(value = """
        SELECT c.name                        AS categoryName,
               SUM(oi.quantity * oi.price)   AS totalRevenue
        FROM order_items oi
        JOIN products p   ON oi.product_id  = p.product_id
        JOIN categories c ON p.category_id  = c.category_id
        JOIN orders o     ON oi.order_id    = o.order_id
        WHERE o.created_at BETWEEN :start AND :end
        GROUP BY c.category_id, c.name
        ORDER BY totalRevenue DESC
        """, nativeQuery = true)
    List<Object[]> findRevenueByCategory(@Param("start") String start,
                                         @Param("end") String end);

    /**
     * Native MySQL query: Full order line detail for the detailed report sheet.
     * JOINs 4 tables: order_items → orders → users → products
     * Returns every individual sale line with customer info.
     * Demonstrates DBMS: 4-table JOIN, aliasing, ORDER BY date.
     */
    @Query(value = """
        SELECT o.order_id            AS orderId,
               o.created_at          AS orderDate,
               u.name                AS customerName,
               u.email               AS customerEmail,
               p.name                AS productName,
               c.name                AS categoryName,
               oi.quantity           AS quantity,
               oi.price              AS unitPrice,
               (oi.quantity * oi.price) AS lineTotal,
               o.payment_method      AS paymentMethod,
               o.status              AS orderStatus,
               o.is_bulk_order       AS isBulkOrder,
               o.discount_percentage AS discountPct,
               o.discount_amount     AS discountAmt,
               CONCAT(a.address_line1, ', ', a.city, ', ', a.state, ' - ', a.pincode) AS shippingAddress
        FROM order_items oi
        JOIN orders o     ON oi.order_id   = o.order_id
        JOIN users u      ON o.user_id     = u.user_id
        JOIN products p   ON oi.product_id = p.product_id
        JOIN categories c ON p.category_id = c.category_id
        JOIN addresses a  ON o.shipping_address_id  = a.address_id
        WHERE o.created_at BETWEEN :start AND :end
        ORDER BY o.created_at DESC, o.order_id
        """, nativeQuery = true)
    List<Object[]> findDetailedOrderItems(@Param("start") String start,
                                          @Param("end") String end);

    @Query(value = """
        SELECT p.product_id      AS productId,
               p.name            AS productName,
               c.name            AS categoryName,
               SUM(oi.quantity)  AS totalSold,
               SUM(oi.quantity * oi.price) AS totalRevenue
        FROM order_items oi
        JOIN products p   ON oi.product_id = p.product_id
        JOIN categories c ON p.category_id = c.category_id
        JOIN orders o     ON oi.order_id   = o.order_id
        WHERE o.created_at BETWEEN :start AND :end
        GROUP BY p.product_id, p.name, c.name
        ORDER BY totalSold DESC
        """, nativeQuery = true)
    List<Object[]> findProductSalesAnalysis(@Param("start") String start,
                                            @Param("end") String end);

    void deleteByOrderOrderId(Long orderId);
}
