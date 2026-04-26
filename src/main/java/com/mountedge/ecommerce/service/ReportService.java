package com.mountedge.ecommerce.service;

import com.mountedge.ecommerce.dto.AnalyticsDto;
import com.mountedge.ecommerce.entity.User;
import com.mountedge.ecommerce.repository.OrderItemRepository;
import com.mountedge.ecommerce.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ReportService - Demonstrates OOP principles:
 *   - Abstraction: generateSalesReport() hides complex Excel-building logic.
 *   - Encapsulation: Each sheet built by a dedicated private helper method.
 *   - Single Responsibility: Only responsible for report generation.
 *   - Composition: Depends on AnalyticsService, OrderItemRepository, UserRepository.
 *
 * Generates a 6-sheet .xlsx report:
 *   1. Executive Summary
 *   2. Detailed Sales (every line item with customer info)
 *   3. Monthly Breakdown
 *   4. Top Products
 *   5. Category Sales
 *   6. Registered Users
 */
@Service
public class ReportService {

    private final AnalyticsService analyticsService;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    public ReportService(AnalyticsService analyticsService,
                         OrderItemRepository orderItemRepository,
                         UserRepository userRepository) {
        this.analyticsService = analyticsService;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generateSalesReport(LocalDate startDate, LocalDate endDate) throws IOException {
        AnalyticsDto data = analyticsService.getAnalytics(startDate, endDate);
        String start = startDate.atStartOfDay().toString().replace("T", " ");
        String end   = endDate.plusDays(1).atStartOfDay().toString().replace("T", " ");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Create shared styles
            CellStyle titleStyle    = createTitleStyle(workbook);
            CellStyle headerStyle   = createHeaderStyle(workbook);
            CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle     = createDateStyle(workbook);
            CellStyle altRowStyle   = createAltRowStyle(workbook);

            buildSummarySheet(workbook, data, startDate, endDate, titleStyle, headerStyle, subHeaderStyle, currencyStyle);
            buildDetailedSalesSheet(workbook, start, end, headerStyle, currencyStyle, dateStyle, altRowStyle);
            buildMonthlySheet(workbook, data, headerStyle, currencyStyle);
            buildTopProductsSheet(workbook, data, headerStyle);
            buildProductAnalysisSheet(workbook, data, headerStyle, currencyStyle);
            buildAuditSheet(workbook, data, headerStyle, dateStyle);
            buildCategorySheet(workbook, data, headerStyle, currencyStyle);
            buildUsersSheet(workbook, headerStyle, altRowStyle);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ─── Sheet 1: Executive Summary ───────────────────────────────────────────
    private void buildSummarySheet(XSSFWorkbook wb, AnalyticsDto data,
                                   LocalDate startDate, LocalDate endDate,
                                   CellStyle titleStyle, CellStyle headerStyle,
                                   CellStyle subHeaderStyle, CellStyle currencyStyle) {
        Sheet sheet = wb.createSheet("Executive Summary");
        sheet.setColumnWidth(0, 10000);
        sheet.setColumnWidth(1, 7000);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

        Row title = sheet.createRow(0);
        Cell tc = title.createCell(0);
        tc.setCellValue("MountEdge — Sales & Profit Report");
        tc.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        Row dateRow = sheet.createRow(1);
        dateRow.createCell(0).setCellValue("Report Period:");
        dateRow.createCell(1).setCellValue(startDate.format(fmt) + " → " + endDate.format(fmt));

        sheet.createRow(2);

        Row kpiHeader = sheet.createRow(3);
        Cell kh = kpiHeader.createCell(0);
        kh.setCellValue("Key Performance Indicators");
        kh.setCellStyle(subHeaderStyle);

        String[][] kpis = {
            {"Total Revenue (₹)",       data.getTotalRevenue().toPlainString()},
            {"Total Orders",            String.valueOf(data.getTotalOrders())},
            {"Total Products Sold",     String.valueOf(data.getTotalProductsSold())},
            {"Average Order Value (₹)", data.getAverageOrderValue().toPlainString()}
        };
        for (int i = 0; i < kpis.length; i++) {
            Row row = sheet.createRow(4 + i);
            Cell lbl = row.createCell(0);
            lbl.setCellValue(kpis[i][0]);
            lbl.setCellStyle(headerStyle);
            row.createCell(1).setCellValue(kpis[i][1]);
        }
    }

    // ─── Sheet 2: Detailed Sales (all line items with customer info) ──────────
    private void buildDetailedSalesSheet(XSSFWorkbook wb,
                                         String start, String end,
                                         CellStyle headerStyle,
                                         CellStyle currencyStyle,
                                         CellStyle dateStyle,
                                         CellStyle altRowStyle) {
        Sheet sheet = wb.createSheet("Detailed Sales");
        sheet.setColumnWidth(0, 3500);   // Order ID
        sheet.setColumnWidth(1, 7000);   // Date
        sheet.setColumnWidth(2, 6000);   // Customer Name
        sheet.setColumnWidth(3, 8000);   // Customer Email
        sheet.setColumnWidth(4, 9000);   // Product Name
        sheet.setColumnWidth(5, 5000);   // Category
        sheet.setColumnWidth(6, 3500);   // Qty
        sheet.setColumnWidth(7, 5000);   // Unit Price
        sheet.setColumnWidth(8, 5500);   // Line Total
        sheet.setColumnWidth(9, 5500);   // Payment
        sheet.setColumnWidth(10, 4500);  // Status
        sheet.setColumnWidth(11, 3500);  // Bulk?
        sheet.setColumnWidth(12, 3500);  // Disc %
        sheet.setColumnWidth(13, 4000);  // Disc Amt
        sheet.setColumnWidth(14, 12000); // Shipping Address

        String[] headers = {"Order ID", "Date", "Customer", "Email", "Product", "Category", "Qty", "Unit Price (₹)", "Line Total (₹)", "Payment", "Status", "Bulk Order?", "Discount %", "Discount Amount (₹)", "Shipping Address"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(headerRow, i, headers[i], headerStyle);
        }

        List<Object[]> rows = orderItemRepository.findDetailedOrderItems(start, end);
        for (int i = 0; i < rows.size(); i++) {
            Object[] r = rows.get(i);
            Row row = sheet.createRow(i + 1);
            CellStyle rowStyle = (i % 2 == 1) ? altRowStyle : null;

            setCell(row, 0, r[0], null);          // orderId
            setCell(row, 1, r[1], null);          // orderDate (String)
            setCell(row, 2, r[2], rowStyle);       // customerName
            setCell(row, 3, r[3], rowStyle);       // customerEmail
            setCell(row, 4, r[4], rowStyle);       // productName
            setCell(row, 5, r[5], rowStyle);       // categoryName
            setCell(row, 6, r[6], null);           // quantity
            setNumericCell(row, 7, r[7], currencyStyle);  // unitPrice
            setNumericCell(row, 8, r[8], currencyStyle);  // lineTotal
            setCell(row, 9, r[9], rowStyle);       // paymentMethod
            setCell(row, 10, r[10], rowStyle);     // status
            setCell(row, 11, Boolean.TRUE.equals(r[11]) ? "YES" : "NO", rowStyle); // isBulkOrder
            setNumericCell(row, 12, r[12], null);  // discountPct
            setNumericCell(row, 13, r[13], currencyStyle); // discountAmt
            setCell(row, 14, r[14], rowStyle);     // shippingAddress
        }

        // Freeze header row
        sheet.createFreezePane(0, 1);
        // Auto-filter
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headers.length - 1));
    }

    // ─── Sheet 3: Monthly Breakdown ───────────────────────────────────────────
    private void buildMonthlySheet(XSSFWorkbook wb, AnalyticsDto data,
                                   CellStyle headerStyle, CellStyle currencyStyle) {
        Sheet sheet = wb.createSheet("Monthly Breakdown");
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 6000);
        sheet.setColumnWidth(2, 4000);

        Row header = sheet.createRow(0);
        createHeaderCell(header, 0, "Month", headerStyle);
        createHeaderCell(header, 1, "Revenue (₹)", headerStyle);
        createHeaderCell(header, 2, "Orders", headerStyle);

        List<String> labels = data.getMonthLabels();
        List<BigDecimal> revenues = data.getMonthlyRevenue();
        List<Long> orders = data.getMonthlyOrders();

        for (int i = 0; i < labels.size(); i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(labels.get(i));
            Cell rev = row.createCell(1);
            rev.setCellValue(revenues.get(i).doubleValue());
            rev.setCellStyle(currencyStyle);
            row.createCell(2).setCellValue(orders.get(i));
        }
    }

    // ─── Sheet 4: Top Selling Products ────────────────────────────────────────
    private void buildTopProductsSheet(XSSFWorkbook wb, AnalyticsDto data,
                                       CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Top Products");
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 10000);
        sheet.setColumnWidth(2, 5000);

        Row header = sheet.createRow(0);
        createHeaderCell(header, 0, "Rank", headerStyle);
        createHeaderCell(header, 1, "Product Name", headerStyle);
        createHeaderCell(header, 2, "Units Sold", headerStyle);

        List<String> names = data.getTopProductNames();
        List<Long> sales = data.getTopProductSales();
        for (int i = 0; i < names.size(); i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(i + 1);
            row.createCell(1).setCellValue(names.get(i));
            row.createCell(2).setCellValue(sales.get(i));
        }
    }

    // ─── Sheet 4B: Comprehensive Product Sales Analysis ───────────────────────
    private void buildProductAnalysisSheet(XSSFWorkbook wb, AnalyticsDto data,
                                           CellStyle headerStyle, CellStyle currencyStyle) {
        Sheet sheet = wb.createSheet("Product Sales Analysis");
        sheet.setColumnWidth(0, 3000);   // ID
        sheet.setColumnWidth(1, 10000);  // Name
        sheet.setColumnWidth(2, 6000);   // Category
        sheet.setColumnWidth(3, 4000);   // Units Sold
        sheet.setColumnWidth(4, 6000);   // Revenue

        String[] headers = {"Product ID", "Product Name", "Category", "Units Sold", "Total Revenue (₹)"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(headerRow, i, headers[i], headerStyle);
        }

        List<Object[]> analysis = data.getProductSalesAnalysis();
        if (analysis != null) {
            for (int i = 0; i < analysis.size(); i++) {
                Object[] r = analysis.get(i);
                Row row = sheet.createRow(i + 1);
                setCell(row, 0, r[0], null);                 // productId
                setCell(row, 1, r[1], null);                 // productName
                setCell(row, 2, r[2], null);                 // categoryName
                setCell(row, 3, r[3], null);                 // totalSold
                setNumericCell(row, 4, r[4], currencyStyle); // totalRevenue
            }
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headers.length - 1));
    }

    // ─── Sheet 7: Order Activity Audit Trail ──────────────────────────────────
    private void buildAuditSheet(XSSFWorkbook wb, AnalyticsDto data,
                                 CellStyle headerStyle, CellStyle dateStyle) {
        Sheet sheet = wb.createSheet("Order Activity Audit");
        sheet.setColumnWidth(0, 3500);   // Order ID
        sheet.setColumnWidth(1, 6000);   // Customer
        sheet.setColumnWidth(2, 5000);   // Status
        sheet.setColumnWidth(3, 7000);   // Timestamp
        sheet.setColumnWidth(4, 12000);  // Admin Notes / Activity Details

        String[] headers = {"Order ID", "Customer", "New Status", "Timestamp", "Activity Details / Notes"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(headerRow, i, headers[i], headerStyle);
        }

        List<Object[]> audit = data.getOrderAuditTrail();
        if (audit != null) {
            for (int i = 0; i < audit.size(); i++) {
                Object[] r = audit.get(i);
                Row row = sheet.createRow(i + 1);
                setCell(row, 0, r[0], null);           // orderId
                setCell(row, 1, r[4], null);           // customerName
                setCell(row, 2, r[1], null);           // status
                setCell(row, 3, r[2], dateStyle);      // timestamp
                setCell(row, 4, r[3], null);           // notes
            }
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headers.length - 1));
    }

    // ─── Sheet 5: Category Sales ──────────────────────────────────────────────
    private void buildCategorySheet(XSSFWorkbook wb, AnalyticsDto data,
                                    CellStyle headerStyle, CellStyle currencyStyle) {
        Sheet sheet = wb.createSheet("Category Sales");
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 6000);

        Row header = sheet.createRow(0);
        createHeaderCell(header, 0, "Category", headerStyle);
        createHeaderCell(header, 1, "Total Revenue (₹)", headerStyle);

        List<String> names = data.getCategoryNames();
        List<BigDecimal> revenues = data.getCategoryRevenue();
        for (int i = 0; i < names.size(); i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(names.get(i));
            Cell rev = row.createCell(1);
            rev.setCellValue(revenues.get(i).doubleValue());
            rev.setCellStyle(currencyStyle);
        }
    }

    // ─── Sheet 6: Registered Users ────────────────────────────────────────────
    private void buildUsersSheet(XSSFWorkbook wb, CellStyle headerStyle, CellStyle altRowStyle) {
        Sheet sheet = wb.createSheet("Registered Users");
        sheet.setColumnWidth(0, 3500);   // ID
        sheet.setColumnWidth(1, 8000);   // Name
        sheet.setColumnWidth(2, 10000);  // Email
        sheet.setColumnWidth(3, 5000);   // Role

        Row header = sheet.createRow(0);
        createHeaderCell(header, 0, "User ID", headerStyle);
        createHeaderCell(header, 1, "Name", headerStyle);
        createHeaderCell(header, 2, "Email", headerStyle);
        createHeaderCell(header, 3, "Role", headerStyle);

        List<User> users = userRepository.findAll();
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            Row row = sheet.createRow(i + 1);
            if (i % 2 == 1) {
                for (int c = 0; c < 4; c++) row.createCell(c).setCellStyle(altRowStyle);
            }
            row.createCell(0).setCellValue(u.getUserId());
            row.createCell(1).setCellValue(u.getName());
            row.createCell(2).setCellValue(u.getEmail());
            row.createCell(3).setCellValue(u.getRole());
        }
        sheet.createFreezePane(0, 1);
    }

    // ─── Cell Helpers ─────────────────────────────────────────────────────────

    private void createHeaderCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void setCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value != null) cell.setCellValue(value.toString());
        if (style != null) cell.setCellStyle(style);
    }

    private void setNumericCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value != null) {
            try { cell.setCellValue(Double.parseDouble(value.toString())); }
            catch (NumberFormatException e) { cell.setCellValue(value.toString()); }
        }
        if (style != null) cell.setCellStyle(style);
    }

    // ─── Style Factory Methods ────────────────────────────────────────────────

    private CellStyle createTitleStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    private CellStyle createSubHeaderStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }

    private CellStyle createCurrencyStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createDateStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("dd-mmm-yyyy hh:mm"));
        return style;
    }

    private CellStyle createAltRowStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
