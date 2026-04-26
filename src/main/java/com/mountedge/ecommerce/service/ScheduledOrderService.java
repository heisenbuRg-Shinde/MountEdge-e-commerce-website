package com.mountedge.ecommerce.service;

import com.mountedge.ecommerce.dto.ScheduledOrderDto;
import com.mountedge.ecommerce.entity.*;
import com.mountedge.ecommerce.exception.BadRequestException;
import com.mountedge.ecommerce.exception.ResourceNotFoundException;
import com.mountedge.ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduledOrderService {

    private final ScheduledOrderRepository scheduledOrderRepository;
    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final OrderService orderService;

    public ScheduledOrderService(ScheduledOrderRepository scheduledOrderRepository, CartService cartService, AddressRepository addressRepository, OrderService orderService) {
        this.scheduledOrderRepository = scheduledOrderRepository;
        this.cartService = cartService;
        this.addressRepository = addressRepository;
        this.orderService = orderService;
    }

    @Transactional
    public ScheduledOrderDto createFromCart(Long userId, Long addressId, String paymentMethodStr, Integer dayOfMonth, String notes) {
        if (dayOfMonth < 1 || dayOfMonth > 28) {
            throw new BadRequestException("Day of month must be between 1 and 28");
        }

        Cart cart = cartService.getCartByUserId(userId);
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("Invalid address for user");
        }

        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid payment method");
        }

        LocalDate nextRunDate = calculateNextRunDate(dayOfMonth);

        ScheduledOrder scheduledOrder = new ScheduledOrder(cart.getUser(), ScheduledOrderStatus.ACTIVE, dayOfMonth, nextRunDate, paymentMethod, address, notes);

        for (CartItem item : cart.getItems()) {
            ScheduledOrderItem scheduledItem = new ScheduledOrderItem(scheduledOrder, item.getProduct(), item.getQuantity());
            scheduledOrder.addItem(scheduledItem);
        }

        ScheduledOrder savedOrder = scheduledOrderRepository.save(scheduledOrder);
        
        // After successfully scheduling, place the first order immediately if they selected to subscribe from checkout.
        // Wait, the plan was: "When toggled, Place Order becomes Subscribe & Place First Order".
        // Let's create the first order right now.
        orderService.checkoutScheduledOrder(savedOrder);
        
        // After placing the first order, we clear the cart
        cartService.clearCart(cart);

        return toDto(savedOrder);
    }

    private LocalDate calculateNextRunDate(Integer dayOfMonth) {
        LocalDate today = LocalDate.now();
        LocalDate nextDate = today.withDayOfMonth(dayOfMonth);
        // Since we are placing the first order immediately, the next run date is always next month.
        return nextDate.plusMonths(1);
    }

    @Transactional(readOnly = true)
    public List<ScheduledOrderDto> getUserOrders(Long userId) {
        return scheduledOrderRepository.findByUserUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ScheduledOrderDto updateStatus(Long id, Long userId, ScheduledOrderStatus newStatus) {
        ScheduledOrder order = scheduledOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled Order not found"));

        if (userId != null && !order.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to scheduled order");
        }

        order.setStatus(newStatus);
        return toDto(scheduledOrderRepository.save(order));
    }

    @Transactional
    public void processDueOrders() {
        LocalDate today = LocalDate.now();
        List<ScheduledOrder> dueOrders = scheduledOrderRepository.findByNextRunDateLessThanEqualAndStatus(today, ScheduledOrderStatus.ACTIVE);
        
        for (ScheduledOrder order : dueOrders) {
            try {
                orderService.checkoutScheduledOrder(order);
                // Advance to next month
                LocalDate nextDate = order.getNextRunDate().plusMonths(1);
                order.setNextRunDate(nextDate);
                scheduledOrderRepository.save(order);
            } catch (Exception e) {
                // Log error but continue with other orders
                System.err.println("Failed to process scheduled order " + order.getScheduledOrderId() + ": " + e.getMessage());
                // Optionally pause the order on failure
                // order.setStatus(ScheduledOrderStatus.PAUSED);
                // scheduledOrderRepository.save(order);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ScheduledOrderDto> adminGetAll() {
        return scheduledOrderRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    private ScheduledOrderDto toDto(ScheduledOrder order) {
        if (order == null) return null;
        
        List<ScheduledOrderDto.ScheduledOrderItemDto> items = order.getItems().stream()
                .map(i -> new ScheduledOrderDto.ScheduledOrderItemDto(i.getProduct().getProductId(), i.getProduct().getName(), i.getQuantity()))
                .collect(Collectors.toList());

        String addressSummary = order.getShippingAddress() != null ? 
                order.getShippingAddress().getAddressLine1() + ", " + order.getShippingAddress().getCity() : "No Address";

        return new ScheduledOrderDto(
                order.getScheduledOrderId(),
                order.getUser().getName(),
                order.getUser().getEmail(),
                order.getStatus().name(),
                order.getDayOfMonth(),
                order.getNextRunDate(),
                order.getPaymentMethod().name(),
                addressSummary,
                order.getNotes(),
                order.getCreatedAt(),
                items
        );
    }
}
