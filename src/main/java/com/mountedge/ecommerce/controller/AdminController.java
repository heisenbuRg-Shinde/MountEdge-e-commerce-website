package com.mountedge.ecommerce.controller;

import com.mountedge.ecommerce.dto.AnalyticsDto;
import com.mountedge.ecommerce.dto.DashboardStatsDto;
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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AnalyticsService analyticsService;
    private final ReportService reportService;
    private final UserRepository userRepository;

    public AdminController(AdminService adminService,
                           AnalyticsService analyticsService,
                           ReportService reportService,
                           UserRepository userRepository) {
        this.adminService = adminService;
        this.analyticsService = analyticsService;
        this.reportService = reportService;
        this.userRepository = userRepository;
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
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
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
}
