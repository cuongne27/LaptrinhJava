package com.evm.backend.service;

import com.evm.backend.dto.request.ReportFilterRequest;
import com.evm.backend.dto.response.*;

public interface ReportService {

    // D.1: Sales Reports
    SalesReportResponse getSalesReport(ReportFilterRequest filter);

    // D.2: Inventory Reports
    InventoryReportResponse getInventoryReport(ReportFilterRequest filter);

    // C.1, C.2: Dealer Performance
    DealerPerformanceResponse getDealerPerformance(Long dealerId, ReportFilterRequest filter);
    java.util.List<DealerPerformanceResponse> getAllDealersPerformance(ReportFilterRequest filter);

    // Revenue Report
    RevenueReportResponse getRevenueReport(ReportFilterRequest filter);

    // Export functions
    byte[] exportSalesReportToExcel(ReportFilterRequest filter);
    byte[] exportInventoryReportToExcel(ReportFilterRequest filter);
    byte[] exportDealerPerformanceToPdf(Long dealerId, ReportFilterRequest filter);
}