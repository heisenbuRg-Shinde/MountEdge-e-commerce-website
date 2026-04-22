package com.mountedge.ecommerce.mapper;

import com.mountedge.ecommerce.dto.OrderSummaryDto;
import com.mountedge.ecommerce.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderSummaryDto toSummaryDto(Order order) {
        if (order == null) {
            return null;
        }

        int itemCount = order.getItems() != null ? order.getItems().size() : 0;
        
        return new OrderSummaryDto(
                order.getOrderId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt(),
                itemCount
        );
    }
}
