package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * C.1, C.2: Dealer Performance Report Response
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DealerPerformanceResponse {
    private Long dealerId;
    private String dealerName;
    private LocalDate fromDate;
    private LocalDate toDate;

    // Sales performance
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private BigDecimal averageOrderValue;

    // KPI tracking
    private BigDecimal salesTarget;
    private BigDecimal achievedSales;
    private BigDecimal achievementRate; // %
    private String performanceLevel; // EXCELLENT, GOOD, AVERAGE, POOR

    // Contract info
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private BigDecimal commissionRate;

    // Detailed metrics
    private List<MonthlyPerformance> monthlyPerformance;
    private List<ProductPerformance> productBreakdown;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthlyPerformance {
        private String month; // 2024-01
        private BigDecimal revenue;
        private Long orderCount;
        private BigDecimal target;
        private BigDecimal achievementRate;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductPerformance {
        private Long productId;
        private String productName;
        private Long unitsSold;
        private BigDecimal revenue;
        private BigDecimal percentage; // % of total
    }
}