package com.mountedge.ecommerce.service;

import com.mountedge.ecommerce.dto.OrderSummaryDto;
import com.mountedge.ecommerce.entity.*;
import com.mountedge.ecommerce.exception.BadRequestException;
import com.mountedge.ecommerce.exception.ResourceNotFoundException;
import com.mountedge.ecommerce.mapper.OrderMapper;
import com.mountedge.ecommerce.repository.AddressRepository;
import com.mountedge.ecommerce.repository.OrderRepository;
import com.mountedge.ecommerce.repository.OrderStatusHistoryRepository;
import com.mountedge.ecommerce.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, PaymentRepository paymentRepository, CartService cartService, AddressRepository addressRepository, OrderStatusHistoryRepository historyRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.cartService = cartService;
        this.addressRepository = addressRepository;
        this.historyRepository = historyRepository;
        this.orderMapper = orderMapper;
    }

    @Transactional
    public OrderSummaryDto checkout(Long userId, Long addressId, String paymentMethodStr) {
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

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getInventory() == null || product.getInventory().getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Not enough stock for: " + product.getName());
            }
            // Reduce stock
            product.getInventory().setStockQuantity(product.getInventory().getStockQuantity() - item.getQuantity());
            
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        Order order = new Order(cart.getUser(), address, paymentMethod, totalAmount, OrderStatus.PLACED);
        
        for (CartItem item : cart.getItems()) {
            OrderItem orderItem = new OrderItem(order, item.getProduct(), item.getQuantity(), item.getProduct().getPrice());
            order.addItem(orderItem);
        }

        OrderStatusHistory history = new OrderStatusHistory(order, OrderStatus.PLACED, "Order placed successfully");
        order.addStatusHistory(history);

        Order savedOrder = orderRepository.save(order);
        
        String transactionRef = paymentMethod == PaymentMethod.COD ? "COD-" + savedOrder.getOrderId() : "ONL-" + System.currentTimeMillis();
        Payment payment = new Payment(savedOrder, paymentMethod == PaymentMethod.COD ? "PENDING" : "SUCCESS", transactionRef);
        paymentRepository.save(payment);

        cartService.clearCart(cart);

        return orderMapper.toSummaryDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> getUserOrders(Long userId, Pageable pageable) {
        // Find all orders for the user (ignoring pagination for simplicity since we added the method to return List)
        // A proper implementation would use findByUserUserIdOrderByCreatedAtDesc with Pageable.
        // But since we just added List<Order>, we'll return a manual page or convert it.
        // Actually, let's just convert the list and wrap it.
        List<Order> orders = orderRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        return new org.springframework.data.domain.PageImpl<>(
                orders.stream().map(orderMapper::toSummaryDto).collect(java.util.stream.Collectors.toList()),
                pageable,
                orders.size()
        );
    }

    @Transactional
    public OrderSummaryDto updateOrderStatus(Long orderId, String statusStr, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status");
        }

        order.setStatus(newStatus);
        OrderStatusHistory history = new OrderStatusHistory(order, newStatus, notes);
        order.addStatusHistory(history);

        if (newStatus == OrderStatus.CANCELLED) {
            // Restore stock
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product.getInventory() != null) {
                    product.getInventory().setStockQuantity(product.getInventory().getStockQuantity() + item.getQuantity());
                }
            }
        }

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toSummaryDto(savedOrder);
    }
}
