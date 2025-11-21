package com.evm.backend.controller;

import com.evm.backend.dto.request.ReportFilterRequest;
import com.evm.backend.dto.response.*;
import com.evm.backend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Report Management", description = "APIs quản lý báo cáo và thống kê")
public class ReportController {

    private final ReportService reportService;

    // =====================================================
    // D.1: SALES REPORTS
    // =====================================================

    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Báo cáo doanh số bán hàng",
            description = "Thống kê doanh thu, đơn hàng, top performers theo thời gian")
    public ResponseEntity<SalesReportResponse> getSalesReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(required = false) Long dealerId,
            @RequestParam(required = false) Long salesPersonId,
            @RequestParam(required = false) String groupBy // DAY, WEEK, MONTH, QUARTER, YEAR
    ) {
        log.info("REST request to get sales report");

        ReportFilterRequest filter = ReportFilterRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .dealerId(dealerId)
                .salesPersonId(salesPersonId)
                .groupBy(groupBy)
                .build();

        SalesReportResponse response = reportService.getSalesReport(filter);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sales/export")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Xuất báo cáo doanh số ra Excel")
    public ResponseEntity<byte[]> exportSalesReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(required = false) Long dealerId,
            @RequestParam(defaultValue = "EXCEL") String format // EXCEL, PDF, CSV
    ) {
        log.info("REST request to export sales report");

        ReportFilterRequest filter = ReportFilterRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .dealerId(dealerId)
                .exportFormat(format)
                .build();

        byte[] fileContent = reportService.exportSalesReportToExcel(filter);

        HttpHeaders headers = new HttpHeaders();
        if ("EXCEL".equals(format)) {
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment",
                    "sales-report-" + LocalDate.now() + ".xlsx");
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }

    // =====================================================
    // D.2: INVENTORY REPORTS
    // =====================================================

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'DEALER_STAFF', 'ADMIN')")
    @Operation(summary = "Báo cáo tồn kho",
            description = "Thống kê tồn kho, cảnh báo hết hàng/tồn kho thấp")
    public ResponseEntity<InventoryReportResponse> getInventoryReport(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long dealerId,
            @RequestParam(required = false) String status // LOW_STOCK, OUT_OF_STOCK
    ) {
        log.info("REST request to get inventory report");

        ReportFilterRequest filter = ReportFilterRequest.builder()
                .productId(productId)
                .dealerId(dealerId)
                .status(status)
                .build();

        InventoryReportResponse response = reportService.getInventoryReport(filter);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/inventory/export")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Xuất báo cáo tồn kho ra Excel")
    public ResponseEntity<byte[]> exportInventoryReport(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long dealerId
    ) {
        log.info("REST request to export inventory report");

        ReportFilterRequest filter = ReportFilterRequest.builder()
                .productId(productId)
                .dealerId(dealerId)
                .build();

        byte[] fileContent = reportService.exportInventoryReportToExcel(filter);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment",
                "inventory-report-" + LocalDate.now() + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }

    // =====================================================
    // C.1, C.2: DEALER PERFORMANCE
    // =====================================================

    @GetMapping("/dealers/{dealerId}/performance")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Báo cáo hiệu suất đại lý",
            description = "Thống kê doanh số, KPI, xếp hạng đại lý")
    public ResponseEntity<DealerPerformanceResponse> getDealerPerformance(
            @PathVariable Long dealerId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("REST request to get dealer performance: {}", dealerId);

        ReportFilterRequest filter = ReportFilterRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        DealerPerformanceResponse response = reportService.getDealerPerformance(dealerId, filter);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dealers/performance")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Báo cáo hiệu suất tất cả đại lý")
    public ResponseEntity<List<DealerPerformanceResponse>> getAllDealersPerformance(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("REST request to get all dealers performance");

        ReportFilterRequest filter = ReportFilterRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        List<DealerPerformanceResponse> response =
                reportService.getAllDealersPerformance(filter);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dealers/{dealerId}/performance/export")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Xuất báo cáo hiệu suất đại lý ra PDF")
    public ResponseEntity<byte[]> exportDealerPerformance(
            @PathVariable Long dealerId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("REST request to export dealer performance: {}", dealerId);

        ReportFilterRequest filter = ReportFilterRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        byte[] pdfContent = reportService.exportDealerPerformanceToPdf(dealerId, filter);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "dealer-performance-" + dealerId + "-" + LocalDate.now() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
    }

    // =====================================================
    // REVENUE REPORT
    // =====================================================

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Báo cáo doanh thu tổng hợp",
            description = "Thống kê doanh thu, thanh toán, tăng trưởng")
    public ResponseEntity<RevenueReportResponse> getRevenueReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(required = false) Long dealerId
    ) {
        log.info("REST request to get revenue report");

        ReportFilterRequest filter = ReportFilterRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .dealerId(dealerId)
                .build();

        RevenueReportResponse response = reportService.getRevenueReport(filter);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // DASHBOARD SUMMARY
    // =====================================================

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('BRAND_MANAGER', 'ADMIN')")
    @Operation(summary = "Dashboard tổng quan",
            description = "Thống kê tổng quan cho dashboard")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(
            Authentication authentication
    ) {
        log.info("REST request to get dashboard summary");

        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);

        ReportFilterRequest filter = ReportFilterRequest.builder()
                .startDate(startOfMonth)
                .endDate(today)
                .build();

        // Aggregate data from multiple reports
        SalesReportResponse salesReport = reportService.getSalesReport(filter);
        InventoryReportResponse inventoryReport = reportService.getInventoryReport(filter);
        RevenueReportResponse revenueReport = reportService.getRevenueReport(filter);

        DashboardSummaryResponse summary = DashboardSummaryResponse.builder()
                .currentDate(today)
                .totalRevenue(salesReport.getTotalRevenue())
                .totalOrders(salesReport.getTotalOrders())
                .growthRate(salesReport.getGrowthRate())
                .lowStockCount(inventoryReport.getLowStockCount())
                .outOfStockCount(inventoryReport.getOutOfStockCount())
                .totalPendingPayment(revenueReport.getTotalPending())
                .topProducts(salesReport.getTopProducts())
                .recentAlerts(inventoryReport.getAlerts())
                .build();

        return ResponseEntity.ok(summary);
    }
}