package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Revenue Report Response (tổng hợp doanh thu)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevenueReportResponse {
    private LocalDate reportDate;
    private LocalDate fromDate;
    private LocalDate toDate;

    // Revenue breakdown
    private BigDecimal totalRevenue;
    private BigDecimal totalBasePrice;
    private BigDecimal totalVat;
    private BigDecimal totalRegistrationFee;
    private BigDecimal totalDiscount;

    // Payment status
    private BigDecimal totalPaid;
    private BigDecimal totalPending;
    private BigDecimal totalOverdue;

    // Comparisons
    private BigDecimal previousPeriodRevenue;
    private BigDecimal growthRate;

    private List<RevenueByCategory> breakdown;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RevenueByCategory {
        private String category; // DEALER, PRODUCT, CUSTOMER_TYPE
        private String categoryName;
        private BigDecimal revenue;
        private Long orderCount;
        private BigDecimal percentage;
    }
}