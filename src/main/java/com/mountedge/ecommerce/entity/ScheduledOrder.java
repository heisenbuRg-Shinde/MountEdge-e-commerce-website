package com.mountedge.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scheduled_orders")
public class ScheduledOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduledOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduledOrderStatus status;

    @Column(name = "day_of_month", nullable = false)
    private Integer dayOfMonth;

    @Column(name = "next_run_date")
    private LocalDate nextRunDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id", nullable = false)
    private Address shippingAddress;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "scheduledOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduledOrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public ScheduledOrder() {
    }

    public ScheduledOrder(User user, ScheduledOrderStatus status, Integer dayOfMonth, LocalDate nextRunDate, PaymentMethod paymentMethod, Address shippingAddress, String notes) {
        this.user = user;
        this.status = status;
        this.dayOfMonth = dayOfMonth;
        this.nextRunDate = nextRunDate;
        this.paymentMethod = paymentMethod;
        this.shippingAddress = shippingAddress;
        this.notes = notes;
    }

    public Long getScheduledOrderId() { return scheduledOrderId; }
    public void setScheduledOrderId(Long scheduledOrderId) { this.scheduledOrderId = scheduledOrderId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ScheduledOrderStatus getStatus() { return status; }
    public void setStatus(ScheduledOrderStatus status) { this.status = status; }

    public Integer getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(Integer dayOfMonth) { this.dayOfMonth = dayOfMonth; }

    public LocalDate getNextRunDate() { return nextRunDate; }
    public void setNextRunDate(LocalDate nextRunDate) { this.nextRunDate = nextRunDate; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public Address getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(Address shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ScheduledOrderItem> getItems() { return items; }
    public void setItems(List<ScheduledOrderItem> items) { this.items = items; }

    public void addItem(ScheduledOrderItem item) {
        items.add(item);
        item.setScheduledOrder(this);
    }
}
