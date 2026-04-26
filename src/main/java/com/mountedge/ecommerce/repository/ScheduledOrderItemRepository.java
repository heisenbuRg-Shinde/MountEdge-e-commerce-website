package com.mountedge.ecommerce.repository;

import com.mountedge.ecommerce.entity.ScheduledOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledOrderItemRepository extends JpaRepository<ScheduledOrderItem, Long> {
    void deleteByScheduledOrderScheduledOrderId(Long scheduledOrderId);
}
