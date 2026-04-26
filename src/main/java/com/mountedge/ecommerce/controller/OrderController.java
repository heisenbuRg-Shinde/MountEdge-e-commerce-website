package com.mountedge.ecommerce.controller;

import com.mountedge.ecommerce.dto.OrderSummaryDto;
import com.mountedge.ecommerce.dto.OrderDetailDto;
import com.mountedge.ecommerce.config.UserDetailsImpl;
import com.mountedge.ecommerce.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderDetailDto> getOrderDetails(
            Authentication authentication,
            @PathVariable Long orderId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        OrderDetailDto order = orderService.getOrderDetails(orderId);
        
        // Security check: only the order owner or an admin can view the invoice
        if (!order.getCustomerEmail().equals(userDetails.getUsername()) && !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(order);
    }
    
    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderSummaryDto> checkout(
            Authentication authentication,
            @RequestParam Long addressId,
            @RequestParam String paymentMethod) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.checkout(userDetails.getId(), addressId, paymentMethod));
    }

    @GetMapping("/bulk-preview")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.Map<String, Object>> getBulkPreview(
            @RequestParam Long productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(orderService.getBulkOrderPreview(productId, quantity));
    }

    @PostMapping("/bulk-request")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderSummaryDto> requestBulkOrder(
            Authentication authentication,
            @RequestParam Long productId,
            @RequestParam int quantity,
            @RequestParam Long addressId,
            @RequestParam String paymentMethod) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.requestBulkOrder(userDetails.getId(), productId, quantity, addressId, paymentMethod));
    }

    @GetMapping("/my-bulk-orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.List<OrderSummaryDto>> getMyBulkOrders(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // Since we didn't write a custom query for this, we can filter existing orders
        // Note: For large scale, a custom JPQL query in repo is better, but this works for now.
        org.springframework.data.domain.Page<OrderSummaryDto> allOrders = orderService.getUserOrders(userDetails.getId(), org.springframework.data.domain.Pageable.unpaged());
        java.util.List<OrderSummaryDto> bulkOrders = allOrders.stream()
                .filter(o -> Boolean.TRUE.equals(o.getIsBulkOrder()))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(bulkOrders);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<OrderSummaryDto>> getMyOrders(
            Authentication authentication,
            Pageable pageable) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.getUserOrders(userDetails.getId(), pageable));
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderSummaryDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status, notes));
    }
}
