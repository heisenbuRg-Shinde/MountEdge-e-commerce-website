package com.mountedge.ecommerce.repository;

import com.mountedge.ecommerce.entity.ScheduledOrder;
import com.mountedge.ecommerce.entity.ScheduledOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduledOrderRepository extends JpaRepository<ScheduledOrder, Long> {
    List<ScheduledOrder> findByUserUserIdOrderByCreatedAtDesc(Long userId);
    List<ScheduledOrder> findByNextRunDateLessThanEqualAndStatus(LocalDate date, ScheduledOrderStatus status);
    void deleteByUserUserId(Long userId);
}
