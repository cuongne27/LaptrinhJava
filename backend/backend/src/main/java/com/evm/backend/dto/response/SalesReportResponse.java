package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * D.1: Sales Report Response
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesReportResponse {
    private LocalDate fromDate;
    private LocalDate toDate;
    private String groupBy;

    // Tổng hợp
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private BigDecimal averageOrderValue;
    private BigDecimal totalDiscount;
    private BigDecimal growthRate; // So với kỳ trước

    // Chi tiết theo thời gian
    private List<SalesDataPoint> salesByPeriod;

    // Top performers
    private List<TopPerformer> topSalesPersons;
    private List<TopPerformer> topDealers;
    private List<TopProduct> topProducts;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SalesDataPoint {
        private LocalDate date;
        private String period; // 2024-W01, 2024-01, 2024-Q1
        private BigDecimal revenue;
        private Long orderCount;
        private BigDecimal averageValue;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopPerformer {
        private Long id;
        private String name;
        private BigDecimal totalRevenue;
        private Long orderCount;
        private BigDecimal achievementRate; // % hoàn thành KPI
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopProduct {
        private Long productId;
        private String productName;
        private Long unitsSold;
        private BigDecimal revenue;
    }
}