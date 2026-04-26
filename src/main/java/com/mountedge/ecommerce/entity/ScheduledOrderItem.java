package com.mountedge.ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "scheduled_order_items")
public class ScheduledOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduled_order_id", nullable = false)
    private ScheduledOrder scheduledOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    public ScheduledOrderItem() {
    }

    public ScheduledOrderItem(ScheduledOrder scheduledOrder, Product product, Integer quantity) {
        this.scheduledOrder = scheduledOrder;
        this.product = product;
        this.quantity = quantity;
    }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public ScheduledOrder getScheduledOrder() { return scheduledOrder; }
    public void setScheduledOrder(ScheduledOrder scheduledOrder) { this.scheduledOrder = scheduledOrder; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
