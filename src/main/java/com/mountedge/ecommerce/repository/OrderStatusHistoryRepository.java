package com.mountedge.ecommerce.repository;

import com.mountedge.ecommerce.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByOrder_OrderIdOrderByCreatedAtDesc(Long orderId);
    void deleteByOrderOrderId(Long orderId);

    @org.springframework.data.jpa.repository.Query(value = """
        SELECT h.order_id     AS orderId,
               h.status       AS status,
               h.created_at   AS timestamp,
               h.notes        AS notes,
               u.name         AS customerName
        FROM order_status_history h
        JOIN orders o ON h.order_id = o.order_id
        JOIN users u  ON o.user_id = u.user_id
        WHERE h.created_at BETWEEN :start AND :end
        ORDER BY h.created_at DESC
        """, nativeQuery = true)
    List<Object[]> findFullOrderAuditTrail(@org.springframework.data.repository.query.Param("start") String start,
                                           @org.springframework.data.repository.query.Param("end") String end);
}
