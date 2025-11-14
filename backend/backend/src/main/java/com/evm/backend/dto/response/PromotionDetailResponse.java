package com.evm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for Promotion detail view
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionDetailResponse {
    private Long id;
    private String promotionCode;
    private String promotionName;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private String conditions;
    private String status; // ACTIVE, UPCOMING, EXPIRED

    // Statistics
    private Long totalUsages;
    private Long daysRemaining;
    private Integer progressPercentage; // % thời gian đã trôi qua

    // Formatted discount display
    private String discountDisplay; // VD: "10%", "5,000,000 VND"
}