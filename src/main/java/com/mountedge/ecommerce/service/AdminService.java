package com.mountedge.ecommerce.service;

import com.mountedge.ecommerce.dto.DashboardStatsDto;
import com.mountedge.ecommerce.dto.UserDetailDto;
import com.mountedge.ecommerce.dto.OrderSummaryDto;
import com.mountedge.ecommerce.entity.Order;
import com.mountedge.ecommerce.entity.User;
import com.mountedge.ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CartRepository cartRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentRepository paymentRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final WishlistRepository wishlistRepository;
    private final AddressRepository addressRepository;

    public AdminService(UserRepository userRepository, OrderRepository orderRepository,
                        ProductRepository productRepository, InventoryRepository inventoryRepository,
                        CartRepository cartRepository, ReviewRepository reviewRepository,
                        PaymentRepository paymentRepository,
                        OrderStatusHistoryRepository orderStatusHistoryRepository,
                        OrderItemRepository orderItemRepository,
                        WishlistRepository wishlistRepository,
                        AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.cartRepository = cartRepository;
        this.reviewRepository = reviewRepository;
        this.paymentRepository = paymentRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.orderItemRepository = orderItemRepository;
        this.wishlistRepository = wishlistRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();
        BigDecimal totalRevenue = orderRepository.sumTotalAmount();
        long lowStockProducts = inventoryRepository.countByStockQuantityLessThan(10);
        return new DashboardStatsDto(totalUsers, totalOrders, totalRevenue, lowStockProducts);
    }

    @Transactional(readOnly = true)
    public UserDetailDto getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders = orderRepository.findByUserUserIdOrderByCreatedAtDesc(userId);

        long totalOrders = orders.size();
        BigDecimal totalSpent = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderSummaryDto> recentOrders = orders.stream()
                .limit(10)
                .map(o -> new OrderSummaryDto(
                        o.getOrderId(),
                        o.getTotalAmount(),
                        o.getStatus().toString(),
                        o.getCreatedAt(),
                        o.getItems().size(),
                        o.getIsBulkOrder(),
                        o.getDiscountPercentage(),
                        o.getDiscountAmount()
                ))
                .toList();

        return new UserDetailDto(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                totalOrders,
                totalSpent,
                recentOrders
        );
    }

    @Transactional
    public void updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    /**
     * Deletes a user and all their associated data in the correct FK order.
     * Demonstrates OOPS principle: encapsulating complex multi-step deletion logic.
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        // 1. Delete reviews by this user
        reviewRepository.deleteByUserUserId(userId);

        // 2. Delete cart (CartItems cascade from Cart entity)
        cartRepository.deleteByUserUserId(userId);

        // 3. For each order: delete payment, order items, status history, then the order
        List<Order> orders = orderRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        for (Order order : orders) {
            Long orderId = order.getOrderId();
            paymentRepository.findByOrderOrderId(orderId)
                    .ifPresent(paymentRepository::delete);
            orderItemRepository.deleteByOrderOrderId(orderId);
            orderStatusHistoryRepository.deleteByOrderOrderId(orderId);
        }
        orderRepository.deleteAll(orders);

        // 4. Delete wishlist (WishlistItems cascade from Wishlist entity)
        wishlistRepository.findByUser_UserId(userId)
                .ifPresent(wishlistRepository::delete);

        // 5. Delete addresses
        addressRepository.deleteByUserUserId(userId);

        // 6. Finally delete the user
        userRepository.deleteById(userId);
    }
}
