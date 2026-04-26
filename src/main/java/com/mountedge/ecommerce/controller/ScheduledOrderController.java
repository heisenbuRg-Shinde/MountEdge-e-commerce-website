package com.mountedge.ecommerce.controller;

import com.mountedge.ecommerce.dto.ScheduledOrderDto;
import com.mountedge.ecommerce.entity.ScheduledOrderStatus;
import com.mountedge.ecommerce.config.UserDetailsImpl;
import com.mountedge.ecommerce.service.ScheduledOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/scheduled-orders")
public class ScheduledOrderController {

    private final ScheduledOrderService scheduledOrderService;

    public ScheduledOrderController(ScheduledOrderService scheduledOrderService) {
        this.scheduledOrderService = scheduledOrderService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ScheduledOrderDto> create(
            Authentication authentication,
            @RequestParam Long addressId,
            @RequestParam String paymentMethod,
            @RequestParam Integer dayOfMonth,
            @RequestParam(required = false) String notes) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(scheduledOrderService.createFromCart(userDetails.getId(), addressId, paymentMethod, dayOfMonth, notes));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScheduledOrderDto>> getMyScheduledOrders(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(scheduledOrderService.getUserOrders(userDetails.getId()));
    }

    @PatchMapping("/{id}/pause")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ScheduledOrderDto> pause(Authentication authentication, @PathVariable Long id) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(scheduledOrderService.updateStatus(id, userDetails.getId(), ScheduledOrderStatus.PAUSED));
    }

    @PatchMapping("/{id}/resume")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ScheduledOrderDto> resume(Authentication authentication, @PathVariable Long id) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(scheduledOrderService.updateStatus(id, userDetails.getId(), ScheduledOrderStatus.ACTIVE));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ScheduledOrderDto> cancel(Authentication authentication, @PathVariable Long id) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(scheduledOrderService.updateStatus(id, userDetails.getId(), ScheduledOrderStatus.CANCELLED));
    }
}
