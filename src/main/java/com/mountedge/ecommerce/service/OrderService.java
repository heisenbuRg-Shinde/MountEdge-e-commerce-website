package com.mountedge.ecommerce.service;

import com.mountedge.ecommerce.dto.OrderDto;
import com.mountedge.ecommerce.entity.*;
import com.mountedge.ecommerce.exception.BadRequestException;
import com.mountedge.ecommerce.exception.ResourceNotFoundException;
import com.mountedge.ecommerce.repository.OrderRepository;
import com.mountedge.ecommerce.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, PaymentRepository paymentRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.cartService = cartService;
    }

    @Transactional
    public OrderDto checkout(Long userId, String tokenRef) {
        Cart cart = cartService.getCartByUserId(userId);
        
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
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

        Order order = new Order(cart.getUser(), totalAmount, "COMPLETED");
        
        for (CartItem item : cart.getItems()) {
            OrderItem orderItem = new OrderItem(order, item.getProduct(), item.getQuantity(), item.getProduct().getPrice());
            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order); // Cascade will save items
        
        Payment payment = new Payment(savedOrder, "SUCCESS", tokenRef);
        paymentRepository.save(payment);

        cartService.clearCart(cart);

        return new OrderDto(savedOrder.getOrderId(), savedOrder.getTotalAmount(), savedOrder.getStatus(), savedOrder.getCreatedAt(), savedOrder.getUser().getName());
    }

    public List<OrderDto> getUserOrders(Long userId) {
        return orderRepository.findByUserUserIdOrderByCreatedAtDesc(userId).stream()
                .map(order -> new OrderDto(order.getOrderId(), order.getTotalAmount(), order.getStatus(), order.getCreatedAt(), order.getUser().getName()))
                .collect(Collectors.toList());
    }
}
