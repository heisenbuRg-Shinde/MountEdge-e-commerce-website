package com.mountedge.ecommerce.controller;

import com.mountedge.ecommerce.dto.ApiResponse;
import com.mountedge.ecommerce.dto.AnalyticsDto;
import com.mountedge.ecommerce.dto.DashboardStatsDto;
import com.mountedge.ecommerce.dto.UserSummaryDto;
import com.mountedge.ecommerce.dto.UserDetailDto;
import com.mountedge.ecommerce.entity.User;
import com.mountedge.ecommerce.repository.UserRepository;
import com.mountedge.ecommerce.service.AdminService;
import com.mountedge.ecommerce.service.AnalyticsService;
import com.mountedge.ecommerce.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.security.core.Authentication;
import com.mountedge.ecommerce.config.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AnalyticsService analyticsService;
    private final ReportService reportService;
    private final UserRepository userRepository;
    private final com.mountedge.ecommerce.service.OrderService orderService;
    private final com.mountedge.ecommerce.service.ScheduledOrderService scheduledOrderService;

    public AdminController(AdminService adminService,
                           AnalyticsService analyticsService,
                           ReportService reportService,
                           UserRepository userRepository,
                           com.mountedge.ecommerce.service.OrderService orderService,
                           com.mountedge.ecommerce.service.ScheduledOrderService scheduledOrderService) {
        this.adminService = adminService;
        this.analyticsService = analyticsService;
        this.reportService = reportService;
        this.userRepository = userRepository;
        this.orderService = orderService;
        this.scheduledOrderService = scheduledOrderService;
    }

    /** Dashboard summary stats */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    /**
     * Returns list of all registered users (name, email, role).
     * Secured to ADMIN only via class-level @PreAuthorize.
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryDto>> getAllUsers() {
        List<UserSummaryDto> users = userRepository.findAll().stream()
                .map(u -> new UserSummaryDto(u.getUserId(), u.getName(), u.getEmail(), u.getRole()))
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDetailDto> getUserDetails(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserDetails(userId));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponse> updateUserRole(@PathVariable Long userId, @RequestBody java.util.Map<String, String> request, Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        if (currentUser.getId().equals(userId)) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "You cannot change your own role"));
        }
        adminService.updateUserRole(userId, request.get("role"));
        return ResponseEntity.ok(new ApiResponse(true, "User role updated successfully"));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId, Authentication authentication) {
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        if (currentUser.getId().equals(userId)) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "You cannot delete your own admin account"));
        }
        adminService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully"));
    }

    /**
     * Analytics data endpoint for Chart.js charts.
     * Accepts optional date range query params (defaults: last 6 months).
     */
    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsDto> getAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) startDate = LocalDate.now().minusMonths(6);
        if (endDate == null)   endDate   = LocalDate.now();

        return ResponseEntity.ok(analyticsService.getAnalytics(startDate, endDate));
    }

    /**
     * Report download endpoint — returns a .xlsx file as a download attachment.
     * Demonstrates OOP: Service layer handles all generation logic; controller
     * only handles HTTP concerns (headers, response type).
     */
    @GetMapping("/reports/sales")
    public ResponseEntity<byte[]> downloadSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
            throws IOException {

        if (startDate == null) startDate = LocalDate.now().minusMonths(12);
        if (endDate == null)   endDate   = LocalDate.now();

        byte[] report = reportService.generateSalesReport(startDate, endDate);
        String filename = "MountEdge_Sales_Report_" + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(report);
    }

    @GetMapping("/orders/bulk")
    public ResponseEntity<List<com.mountedge.ecommerce.dto.BulkOrderDto>> getBulkOrders() {
        return ResponseEntity.ok(orderService.getAdminBulkOrders());
    }

    @PatchMapping("/orders/bulk/{id}/approve")
    public ResponseEntity<com.mountedge.ecommerce.dto.OrderSummaryDto> approveBulkOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.approveBulkOrder(id));
    }

    @PatchMapping("/orders/bulk/{id}/reject")
    public ResponseEntity<com.mountedge.ecommerce.dto.OrderSummaryDto> rejectBulkOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.rejectBulkOrder(id));
    }

    @GetMapping("/scheduled-orders")
    public ResponseEntity<List<com.mountedge.ecommerce.dto.ScheduledOrderDto>> getAllScheduledOrders() {
        return ResponseEntity.ok(scheduledOrderService.adminGetAll());
    }

    @PatchMapping("/scheduled-orders/{id}/status")
    public ResponseEntity<com.mountedge.ecommerce.dto.ScheduledOrderDto> updateScheduledOrderStatus(
            @PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(scheduledOrderService.updateStatus(id, null, com.mountedge.ecommerce.entity.ScheduledOrderStatus.valueOf(status.toUpperCase())));
    }

    @PostMapping("/scheduled-orders/process")
    public ResponseEntity<ApiResponse> processDueScheduledOrders() {
        scheduledOrderService.processDueOrders();
        return ResponseEntity.ok(new ApiResponse(true, "Successfully processed due scheduled orders."));
    }
}
