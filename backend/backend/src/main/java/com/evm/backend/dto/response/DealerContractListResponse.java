package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for DealerContract list view
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DealerContractListResponse {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal commissionRate;
    private BigDecimal salesTarget;
    private String status; // ACTIVE, EXPIRED, UPCOMING

    // Brand info
    private Integer brandId;
    private String brandName;

    // Dealer info
    private Long dealerId;
    private String dealerName;

    // Calculated fields
    private Long daysRemaining; // Số ngày còn lại (nếu đang active)
    private Long totalDays; // Tổng số ngày hợp đồng
}