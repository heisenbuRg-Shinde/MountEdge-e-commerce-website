package com.mountedge.ecommerce.controller;

import com.mountedge.ecommerce.config.UserDetailsImpl;
import com.mountedge.ecommerce.dto.OrderDto;
import com.mountedge.ecommerce.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderDto> checkout(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @RequestBody Map<String, String> payload) {
        String tokenRef = payload.get("tokenRef");
        return ResponseEntity.ok(orderService.checkout(userDetails.getId(), tokenRef));
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getUserOrders(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(orderService.getUserOrders(userDetails.getId()));
    }
}
