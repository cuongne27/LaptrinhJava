package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for DealerContract detail view
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DealerContractDetailResponse {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String contractTerms;
    private BigDecimal commissionRate;
    private BigDecimal salesTarget;
    private String status; // ACTIVE, EXPIRED, UPCOMING

    // Brand info
    private Integer brandId;
    private String brandName;

    // Dealer info
    private Long dealerId;
    private String dealerName;
    private String dealerAddress;
    private String dealerLevel;

    // Calculated fields
    private Long daysRemaining;
    private Long totalDays;
    private Integer progressPercentage; // % thời gian đã trôi qua
}