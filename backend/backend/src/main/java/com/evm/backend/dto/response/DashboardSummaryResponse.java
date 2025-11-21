package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryResponse {
    private LocalDate currentDate;

    // Sales metrics
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private BigDecimal growthRate;

    // Inventory alerts
    private Integer lowStockCount;
    private Integer outOfStockCount;

    // Payment status
    private BigDecimal totalPendingPayment;

    // Quick insights
    private List<SalesReportResponse.TopProduct> topProducts;
    private List<InventoryReportResponse.StockAlert> recentAlerts;
}