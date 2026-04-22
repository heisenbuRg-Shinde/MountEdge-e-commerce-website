package com.mountedge.ecommerce.service;

import com.mountedge.ecommerce.dto.DashboardStatsDto;
import com.mountedge.ecommerce.repository.OrderRepository;
import com.mountedge.ecommerce.repository.ProductRepository;
import com.mountedge.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public AdminService(UserRepository userRepository, OrderRepository orderRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public DashboardStatsDto getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();
        
        // Sum total amount of all orders
        BigDecimal totalRevenue = orderRepository.findAll().stream()
                .map(order -> order.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count products with stock < 10
        long lowStockProducts = productRepository.findAll().stream()
                .filter(p -> p.getInventory() != null && p.getInventory().getStockQuantity() < 10)
                .count();

        return new DashboardStatsDto(totalUsers, totalOrders, totalRevenue, lowStockProducts);
    }
}
